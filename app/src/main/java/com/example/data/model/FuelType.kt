package com.example.data.model

enum class FuelType(
    val id: String,
    val displayName: String,
    val factor: Double,
    val description: String,
    val stoichAfr: Double
) {
    GASOLINA(
        id = "gasolina",
        displayName = "Gasolina",
        factor = 1.00,
        description = "Estequiometria 14.7:1 • Consumo padrão",
        stoichAfr = 14.7
    ),
    ALCOOL(
        id = "alcool",
        displayName = "Álcool",
        factor = 1.32,
        description = "Etanol E100 / E85 • Estequiometria 9.0:1 (+32%)",
        stoichAfr = 9.0
    ),
    METANOL(
        id = "metanol",
        displayName = "Metanol",
        factor = 1.85,
        description = "Alta performance / Arrancada • Estequiometria 6.4:1 (+85%)",
        stoichAfr = 6.4
    );

    companion object {
        fun fromId(id: String?): FuelType = entries.find { it.id == id } ?: ALCOOL
    }
}
