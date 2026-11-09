package com.example.data.model

data class TunerPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val fuelType: FuelType,
    val injectorRange: InjectorRange,
    val tiBase: Double,
    val mapBase: Double,
    val mode: CalculationMode,
    val maxBoost: Double
) {
    companion object {
        val PRESETS = listOf(
            TunerPreset(
                id = "injectionschool_default",
                name = "Padrão InjectionSchool (Álcool)",
                subtitle = "Álcool • 40-80 lb • 1,55 ms @ -0,60 bar • 1,20 bar turbo",
                fuelType = FuelType.ALCOOL,
                injectorRange = InjectorRange.DE_40_A_80,
                tiBase = 1.55,
                mapBase = -0.60,
                mode = CalculationMode.TURBO_MAP,
                maxBoost = 1.20
            ),
            TunerPreset(
                id = "street_turbo_gasolina",
                name = "Street Turbo (Gasolina)",
                subtitle = "Gasolina • 40-80 lb • 1,30 ms @ -0,55 bar • 1,00 bar turbo",
                fuelType = FuelType.GASOLINA,
                injectorRange = InjectorRange.DE_40_A_80,
                tiBase = 1.30,
                mapBase = -0.55,
                mode = CalculationMode.TURBO_MAP,
                maxBoost = 1.00
            ),
            TunerPreset(
                id = "drag_race_methanol",
                name = "Arrancada / Pista (Metanol)",
                subtitle = "Metanol • >120 lb • 2,20 ms @ -0,50 bar • 2,00 bar turbo",
                fuelType = FuelType.METANOL,
                injectorRange = InjectorRange.ACIMA_120,
                tiBase = 2.20,
                mapBase = -0.50,
                mode = CalculationMode.TURBO_MAP,
                maxBoost = 2.00
            ),
            TunerPreset(
                id = "aspirado_street",
                name = "Aspirado de Rua (Álcool)",
                subtitle = "Álcool • Até 40 lb • 1,70 ms @ -0,65 bar • Aspirado MAP",
                fuelType = FuelType.ALCOOL,
                injectorRange = InjectorRange.ATE_40,
                tiBase = 1.70,
                mapBase = -0.65,
                mode = CalculationMode.ASPIRADO_MAP,
                maxBoost = 0.00
            ),
            TunerPreset(
                id = "aspirado_tps_itb",
                name = "Aspirado Corpos Individuais (TPS)",
                subtitle = "Gasolina • Até 40 lb • 1,40 ms na lenta • Aspirado TPS",
                fuelType = FuelType.GASOLINA,
                injectorRange = InjectorRange.ATE_40,
                tiBase = 1.40,
                mapBase = 0.0,
                mode = CalculationMode.ASPIRADO_TPS,
                maxBoost = 0.00
            )
        )
    }
}
