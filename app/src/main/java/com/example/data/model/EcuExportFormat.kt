package com.example.data.model

import java.util.Locale

enum class EcuExportFormat(val displayName: String, val fileExtension: String) {
    TAB_SEPARATED("ECU Padrão (Tab / InjePro / FuelTech)", "txt") {
        override fun format(points: List<EcuPoint>, mode: CalculationMode, fuel: FuelType, inj: InjectorRange): String {
            val sb = StringBuilder()
            val headerCol = if (mode == CalculationMode.ASPIRADO_TPS) "TPS (%)" else "MAP (bar)"
            sb.append("$headerCol\tTempo (ms)\n")
            for (p in points) {
                sb.append("${p.formattedLoad}\t${p.formattedTime}\n")
            }
            return sb.toString().trimEnd()
        }
    },
    COMMA_SEPARATED("Planilha CSV", "csv") {
        override fun format(points: List<EcuPoint>, mode: CalculationMode, fuel: FuelType, inj: InjectorRange): String {
            val sb = StringBuilder()
            val headerCol = if (mode == CalculationMode.ASPIRADO_TPS) "TPS_Percent" else "MAP_Bar"
            sb.append("$headerCol,Injection_Time_ms\n")
            for (p in points) {
                sb.append("${String.format(Locale.US, "%.2f", p.loadValue)},${String.format(Locale.US, "%.2f", p.injectionTimeMs)}\n")
            }
            return sb.toString().trimEnd()
        }
    },
    ECU_TABLE_ONLY("Apenas Valores (Colar na ECU)", "txt") {
        override fun format(points: List<EcuPoint>, mode: CalculationMode, fuel: FuelType, inj: InjectorRange): String {
            return points.joinToString("\n") { it.formattedTime }
        }
    },
    DETAILED_REPORT("Relatório Completo de Calibração", "txt") {
        override fun format(points: List<EcuPoint>, mode: CalculationMode, fuel: FuelType, inj: InjectorRange): String {
            val sb = StringBuilder()
            sb.append("=== MAPA BASE DE INJEÇÃO ELETRÔNICA ===\n")
            sb.append("Modo: ${mode.title}\n")
            sb.append("Combustível: ${fuel.displayName} (${fuel.description})\n")
            sb.append("Injetores: ${inj.displayName} Lbs/h (${inj.description})\n")
            sb.append("---------------------------------------\n")
            val idlePoint = points.find { it.isReference } ?: points.firstOrNull()
            val atmoPoint = points.find { it.isAtmospheric }
            val peakPoint = points.find { it.isPeak } ?: points.lastOrNull()
            if (idlePoint != null) {
                sb.append("• Marcha Lenta: ${idlePoint.formattedLoad} ${mode.unit} -> ${idlePoint.formattedTime} ms\n")
            }
            if (atmoPoint != null) {
                sb.append("• Atmosférico: 0,00 bar -> ${atmoPoint.formattedTime} ms\n")
            }
            if (peakPoint != null) {
                sb.append("• Pressão Máxima: ${peakPoint.formattedLoad} ${mode.unit} -> ${peakPoint.formattedTime} ms\n")
            }
            sb.append("---------------------------------------\n")
            val headerCol = if (mode == CalculationMode.ASPIRADO_TPS) "TPS (%)" else "MAP (bar)"
            sb.append(String.format("%-12s | %-12s\n", headerCol, "Tempo (ms)"))
            sb.append("---------------------------------------\n")
            for (p in points) {
                sb.append(String.format("%-12s | %-12s\n", p.formattedLoad, p.formattedTime))
            }
            sb.append("=======================================\n")
            sb.append("Gerado por Mapa Base Turbo • AI Studio\n")
            return sb.toString()
        }
    };

    abstract fun format(
        points: List<EcuPoint>,
        mode: CalculationMode,
        fuel: FuelType,
        inj: InjectorRange
    ): String
}
