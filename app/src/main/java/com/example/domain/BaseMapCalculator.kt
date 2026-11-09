package com.example.domain

import com.example.data.model.CalculationMode
import com.example.data.model.EcuPoint
import com.example.data.model.FuelType
import com.example.data.model.InjectorRange
import java.util.Locale
import kotlin.math.max
import kotlin.math.round

object BaseMapCalculator {

    // Standard manifold pressure points for Turbo MAP
    val STANDARD_TURBO_PRESSURES = listOf(
        -1.00, -0.90, -0.80, -0.70, -0.60, -0.50, -0.40, -0.30, -0.20, -0.10,
        0.00, 0.20, 0.40, 0.60, 0.80, 1.00, 1.20
    )

    val EXTENDED_BOOST_STEPS = listOf(1.40, 1.60, 1.80, 2.00, 2.20, 2.40, 2.50)

    // Naturally Aspirated MAP pressures: pure vacuum (depressão) up to atmospheric (0.00 bar)
    val ASPIRADO_MAP_PRESSURES = listOf(
        -1.00, -0.90, -0.80, -0.70, -0.60, -0.50, -0.40, -0.30, -0.20, -0.10, 0.00
    )

    // Standard ECU throttle position breakpoints for Naturally Aspirated TPS / ITBs
    val TPS_POINTS = listOf(
        0.0, 2.0, 5.0, 10.0, 15.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0
    )

    /**
     * Calculates the full calibration curve points based on input parameters and active calculation mode.
     */
    fun calculate(
        fuelType: FuelType,
        injectorRange: InjectorRange,
        tiBase: Double,
        mapBase: Double,
        mode: CalculationMode,
        maxBoost: Double = 1.20,
        targetRpm: Int = 6500
    ): List<EcuPoint> {
        val safeTiBase = max(0.50, tiBase)
        val safeMapBase = mapBase.coerceIn(-1.00, 0.00)

        return when (mode) {
            CalculationMode.TURBO_MAP -> calculateTurboMap(
                fuelType = fuelType,
                injectorRange = injectorRange,
                tiBase = safeTiBase,
                mapBase = safeMapBase,
                maxBoost = maxBoost,
                targetRpm = targetRpm
            )
            CalculationMode.ASPIRADO_MAP -> calculateAspiradoMap(
                fuelType = fuelType,
                injectorRange = injectorRange,
                tiBase = safeTiBase,
                mapBase = safeMapBase,
                targetRpm = targetRpm
            )
            CalculationMode.ASPIRADO_TPS -> calculateAspiradoTps(
                fuelType = fuelType,
                injectorRange = injectorRange,
                tiBase = safeTiBase,
                targetRpm = targetRpm
            )
        }
    }

    private fun calculateTurboMap(
        fuelType: FuelType,
        injectorRange: InjectorRange,
        tiBase: Double,
        mapBase: Double,
        maxBoost: Double,
        targetRpm: Int
    ): List<EcuPoint> {
        val pressures = mutableListOf<Double>()
        pressures.addAll(STANDARD_TURBO_PRESSURES.filter { it <= maxBoost })
        for (extra in EXTENDED_BOOST_STEPS) {
            if (extra <= maxBoost && !pressures.contains(extra)) {
                pressures.add(extra)
            }
        }
        pressures.sort()

        // Calibrated baseline factors:
        // When Alcohol (factor 1.32) & Inj 40-80 (factor 1.0) & 1.55 ms @ -0.60 bar:
        // Result is exactly 1.55 ms at -0.60, 5.42 ms at 0.00 bar, 9.76 ms at 1.20 bar.
        val fuelFactorRatio = fuelType.factor / 1.32
        val injFactorRatio = injectorRange.factor / 1.00

        val nominalAtmoDelta = 3.87 * fuelFactorRatio * injFactorRatio
        val atmoTi = tiBase + nominalAtmoDelta

        val nominalBoostSlopePerBar = (4.34 / 1.20) * fuelFactorRatio * injFactorRatio
        val peakPressure = pressures.lastOrNull() ?: 1.20

        return pressures.map { p ->
            val ti = when {
                p <= mapBase -> tiBase
                p <= 0.00 -> {
                    val progress = (p - mapBase) / (0.00 - mapBase).coerceAtLeast(0.01)
                    tiBase + (progress * nominalAtmoDelta)
                }
                else -> {
                    atmoTi + (p * nominalBoostSlopePerBar)
                }
            }

            val roundedTi = round(ti * 100.0) / 100.0
            val dutyCycle = calculateDutyCycle(roundedTi, targetRpm)

            val isRef = Math.abs(p - mapBase) < 0.01
            val isAtmo = Math.abs(p) < 0.01
            val isPeak = Math.abs(p - peakPressure) < 0.01

            val stageTag = when {
                isRef -> "LENTA"
                isAtmo -> "ATMO"
                isPeak -> "PICO"
                else -> null
            }

            val label = String.format(Locale.US, "%.2f", p)

            EcuPoint(
                loadValue = p,
                displayLabel = label,
                injectionTimeMs = roundedTi,
                dutyCyclePercent = dutyCycle,
                isReference = isRef,
                isAtmospheric = isAtmo,
                isPeak = isPeak,
                stageTag = stageTag
            )
        }
    }

    /**
     * Naturally Aspirated MAP calculation (Injeção por Vácuo/Depressão do Coletor).
     * Scaled from deep overrun vacuum (-1.00 bar) up to atmospheric pressure (0.00 bar / WOT).
     * Constant idle pulse width below reference idle vacuum (safe deceleration).
     */
    private fun calculateAspiradoMap(
        fuelType: FuelType,
        injectorRange: InjectorRange,
        tiBase: Double,
        mapBase: Double,
        targetRpm: Int
    ): List<EcuPoint> {
        val fuelFactorRatio = fuelType.factor / 1.32
        val injFactorRatio = injectorRange.factor / 1.00
        val nominalAtmoDelta = 3.87 * fuelFactorRatio * injFactorRatio

        return ASPIRADO_MAP_PRESSURES.map { p ->
            val ti = when {
                p <= mapBase -> tiBase
                else -> {
                    // Naturally aspirated filling: progressive cylinder filling toward atmospheric 0.00 bar
                    val progress = (p - mapBase) / (0.00 - mapBase).coerceAtLeast(0.01)
                    tiBase + (progress * nominalAtmoDelta)
                }
            }
            val roundedTi = round(ti * 100.0) / 100.0
            val dutyCycle = calculateDutyCycle(roundedTi, targetRpm)

            val isRef = Math.abs(p - mapBase) < 0.01
            val isCruise = Math.abs(p - (-0.30)) < 0.01
            val isWot = Math.abs(p) < 0.01

            val stageTag = when {
                isRef -> "LENTA"
                isCruise -> "CRUZ."
                isWot -> "WOT"
                else -> null
            }

            EcuPoint(
                loadValue = p,
                displayLabel = String.format(Locale.US, "%.2f", p),
                injectionTimeMs = roundedTi,
                dutyCyclePercent = dutyCycle,
                isReference = isRef,
                isAtmospheric = isCruise,
                isPeak = isWot,
                stageTag = stageTag
            )
        }
    }

    /**
     * Naturally Aspirated TPS calculation (Injeção por Abertura de Borboleta).
     * Ideal for Individual Throttle Bodies (ITB) or aggressive race camshafts with low vacuum.
     * Throttle response follows butterfly opening area progression: steep initial rise, smooth WOT ceiling.
     */
    private fun calculateAspiradoTps(
        fuelType: FuelType,
        injectorRange: InjectorRange,
        tiBase: Double,
        targetRpm: Int
    ): List<EcuPoint> {
        val fuelFactorRatio = fuelType.factor / 1.32
        val injFactorRatio = injectorRange.factor / 1.00

        // In a naturally aspirated engine, wide-open throttle (100% TPS) delivers full atmospheric filling,
        // mirroring the fuel demand of 0.00 bar manifold pressure (nominalAtmoDelta ~ 3.87 ms base).
        val nominalAtmoDelta = 3.87 * fuelFactorRatio * injFactorRatio

        return TPS_POINTS.map { tps ->
            val normalized = tps / 100.0
            // Butterfly valve geometry: rapid increase in annular area at low angles (0-20%),
            // followed by progressive cylinder filling plateau up to 100% (exponent 0.72)
            val flowFraction = Math.pow(normalized, 0.72)
            val ti = tiBase + (flowFraction * nominalAtmoDelta)

            val roundedTi = round(ti * 100.0) / 100.0
            val dutyCycle = calculateDutyCycle(roundedTi, targetRpm)

            val isRef = tps == 0.0
            val isCruise = tps == 20.0
            val isWot = tps == 100.0

            val stageTag = when {
                isRef -> "LENTA"
                isCruise -> "CRUZ."
                isWot -> "WOT"
                else -> null
            }

            EcuPoint(
                loadValue = tps,
                displayLabel = "${tps.toInt()}%",
                injectionTimeMs = roundedTi,
                dutyCyclePercent = dutyCycle,
                isReference = isRef,
                isAtmospheric = isCruise,
                isPeak = isWot,
                stageTag = stageTag
            )
        }
    }

    /**
     * Calculates injector duty cycle percentage at given RPM.
     * Duty Cycle (%) = (Injection Time [ms] * RPM) / 1200
     */
    fun calculateDutyCycle(tiMs: Double, rpm: Int): Double {
        if (rpm <= 0) return 0.0
        val dc = (tiMs * rpm) / 1200.0
        return round(dc * 10.0) / 10.0
    }
}
