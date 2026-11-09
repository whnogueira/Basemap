package com.example.data.model

import java.util.Locale

data class EcuPoint(
    val loadValue: Double,
    val displayLabel: String,
    val injectionTimeMs: Double,
    val dutyCyclePercent: Double,
    val isReference: Boolean = false,
    val isAtmospheric: Boolean = false,
    val isPeak: Boolean = false,
    val stageTag: String? = null
) {
    val formattedTime: String
        get() = String.format(Locale.US, "%.2f", injectionTimeMs).replace('.', ',')

    val formattedLoad: String
        get() = displayLabel.replace('.', ',')
}
