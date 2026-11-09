package com.example.data.model

enum class InjectorRange(
    val id: String,
    val displayName: String,
    val factor: Double,
    val description: String
) {
    ATE_40(
        id = "40",
        displayName = "Até 40",
        factor = 1.05,
        description = "Injetores originais / baixa vazão (até 420 cc/min)"
    ),
    DE_40_A_80(
        id = "40-80",
        displayName = "De 40 a 80",
        factor = 1.00,
        description = "Média vazão (ex: Bosch 42-80 lbs, 440 a 840 cc)"
    ),
    DE_80_A_120(
        id = "80-120",
        displayName = "De 80 a 120",
        factor = 0.96,
        description = "Alta vazão (ex: Deka 80 lbs, Bosch 108 lbs, ~1000 cc)"
    ),
    ACIMA_120(
        id = "120+",
        displayName = "Acima de 120",
        factor = 0.93,
        description = "Extrema vazão (ex: 160 lbs, 220 lbs, 1600+ cc)"
    );

    companion object {
        fun fromId(id: String?): InjectorRange = entries.find { it.id == id } ?: DE_40_A_80
    }
}
