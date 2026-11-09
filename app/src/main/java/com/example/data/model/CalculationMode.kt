package com.example.data.model

enum class CalculationMode(
    val id: String,
    val title: String,
    val unit: String,
    val description: String
) {
    ASPIRADO_TPS(
        id = "tps",
        title = "Aspirado TPS",
        unit = "%",
        description = "Injeção por abertura de borboleta (TPS %)"
    ),
    ASPIRADO_MAP(
        id = "aspmap",
        title = "Aspirado MAP",
        unit = "bar",
        description = "Injeção por depressão do coletor (vácuo até 0.00 bar)"
    ),
    TURBO_MAP(
        id = "turbo",
        title = "Turbo MAP",
        unit = "bar",
        description = "Injeção por sobrealimentação (vácuo + pressão positiva)"
    );

    companion object {
        fun fromId(id: String?): CalculationMode = entries.find { it.id == id } ?: TURBO_MAP
    }
}
