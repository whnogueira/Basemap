package com.example

import com.example.data.model.CalculationMode
import com.example.data.model.FuelType
import com.example.data.model.InjectorRange
import com.example.domain.BaseMapCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBaseMapCalculatorTurboReferenceValues() {
        // Values from user's screenshot: Álcool + De 40 a 80 + 1.55 ms @ -0.60 bar
        val points = BaseMapCalculator.calculate(
            fuelType = FuelType.ALCOOL,
            injectorRange = InjectorRange.DE_40_A_80,
            tiBase = 1.55,
            mapBase = -0.60,
            mode = CalculationMode.TURBO_MAP,
            maxBoost = 1.20,
            targetRpm = 6500
        )

        // Flat in deep vacuum (-1.00 to -0.60)
        val pMinus100 = points.find { Math.abs(it.loadValue - (-1.00)) < 0.01 }
        val pMinus60 = points.find { Math.abs(it.loadValue - (-0.60)) < 0.01 }
        val pZero = points.find { Math.abs(it.loadValue - 0.00) < 0.01 }
        val pPeak = points.find { Math.abs(it.loadValue - 1.20) < 0.01 }

        assertEquals(1.55, pMinus100?.injectionTimeMs ?: 0.0, 0.01)
        assertEquals(1.55, pMinus60?.injectionTimeMs ?: 0.0, 0.01)
        assertEquals(5.42, pZero?.injectionTimeMs ?: 0.0, 0.01)
        assertEquals(9.76, pPeak?.injectionTimeMs ?: 0.0, 0.01)
    }

    @Test
    fun testAspiradoMapCalculation() {
        val points = BaseMapCalculator.calculate(
            fuelType = FuelType.ALCOOL,
            injectorRange = InjectorRange.DE_40_A_80,
            tiBase = 1.55,
            mapBase = -0.60,
            mode = CalculationMode.ASPIRADO_MAP,
            targetRpm = 6500
        )

        // Verifies NA MAP ends at 0.00 bar (atmospheric peak load, no positive boost)
        val pLast = points.last()
        assertEquals(0.00, pLast.loadValue, 0.01)
        assertEquals("WOT", pLast.stageTag)
        assertEquals(5.42, pLast.injectionTimeMs, 0.01)

        // Verifies flat deceleration vacuum below -0.60 bar
        val pIdle = points.find { Math.abs(it.loadValue - (-0.60)) < 0.01 }
        val pDeep = points.find { Math.abs(it.loadValue - (-0.90)) < 0.01 }
        assertEquals(1.55, pIdle?.injectionTimeMs ?: 0.0, 0.01)
        assertEquals(1.55, pDeep?.injectionTimeMs ?: 0.0, 0.01)
        assertEquals("LENTA", pIdle?.stageTag)

        // Verifies progressive mid-load cruising (e.g. at -0.30 bar)
        val pCruise = points.find { Math.abs(it.loadValue - (-0.30)) < 0.01 }
        assertTrue("Cruise time should be between idle and WOT", (pCruise?.injectionTimeMs ?: 0.0) in 1.56..5.41)
        assertEquals("CRUZ.", pCruise?.stageTag)
    }

    @Test
    fun testAspiradoTpsCalculation() {
        val points = BaseMapCalculator.calculate(
            fuelType = FuelType.ALCOOL,
            injectorRange = InjectorRange.DE_40_A_80,
            tiBase = 1.55,
            mapBase = -0.60,
            mode = CalculationMode.ASPIRADO_TPS,
            targetRpm = 6500
        )

        // Verifies TPS starts at 0% and ends at 100%
        val p0 = points.first()
        val p100 = points.last()
        assertEquals(0.0, p0.loadValue, 0.01)
        assertEquals("0%", p0.displayLabel)
        assertEquals(1.55, p0.injectionTimeMs, 0.01)
        assertEquals("LENTA", p0.stageTag)

        assertEquals(100.0, p100.loadValue, 0.01)
        assertEquals("100%", p100.displayLabel)
        assertEquals("WOT", p100.stageTag)
        assertEquals(5.42, p100.injectionTimeMs, 0.02)

        // Verifies progressive throttle response (monotonic increase)
        for (i in 0 until points.size - 1) {
            assertTrue(
                "TPS pulse width should be monotonically increasing with throttle opening",
                points[i + 1].injectionTimeMs >= points[i].injectionTimeMs
            )
        }
    }

    @Test
    fun testFuelCompensationFactors() {
        val gasPoints = BaseMapCalculator.calculate(
            fuelType = FuelType.GASOLINA,
            injectorRange = InjectorRange.DE_40_A_80,
            tiBase = 1.55,
            mapBase = -0.60,
            mode = CalculationMode.TURBO_MAP,
            maxBoost = 1.20
        )
        val alcPoints = BaseMapCalculator.calculate(
            fuelType = FuelType.ALCOOL,
            injectorRange = InjectorRange.DE_40_A_80,
            tiBase = 1.55,
            mapBase = -0.60,
            mode = CalculationMode.TURBO_MAP,
            maxBoost = 1.20
        )
        val metPoints = BaseMapCalculator.calculate(
            fuelType = FuelType.METANOL,
            injectorRange = InjectorRange.DE_40_A_80,
            tiBase = 1.55,
            mapBase = -0.60,
            mode = CalculationMode.TURBO_MAP,
            maxBoost = 1.20
        )

        val gasPeak = gasPoints.last().injectionTimeMs
        val alcPeak = alcPoints.last().injectionTimeMs
        val metPeak = metPoints.last().injectionTimeMs

        assertTrue("Alcohol peak ($alcPeak) must be greater than Gas peak ($gasPeak)", alcPeak > gasPeak)
        assertTrue("Methanol peak ($metPeak) must be greater than Alcohol peak ($alcPeak)", metPeak > alcPeak)
    }

    @Test
    fun testDutyCycleCalculation() {
        val dc = BaseMapCalculator.calculateDutyCycle(10.0, 6000)
        assertEquals(50.0, dc, 0.1)
    }
}
