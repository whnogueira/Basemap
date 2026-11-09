package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CalculationMode
import com.example.data.model.EcuExportFormat
import com.example.data.model.FuelType
import com.example.data.model.InjectorRange

class TunerPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tuner_prefs", Context.MODE_PRIVATE)

    var fuelType: FuelType
        get() = FuelType.fromId(prefs.getString(KEY_FUEL, FuelType.ALCOOL.id))
        set(value) = prefs.edit().putString(KEY_FUEL, value.id).apply()

    var injectorRange: InjectorRange
        get() = InjectorRange.fromId(prefs.getString(KEY_INJECTOR, InjectorRange.DE_40_A_80.id))
        set(value) = prefs.edit().putString(KEY_INJECTOR, value.id).apply()

    var calculationMode: CalculationMode
        get() = CalculationMode.fromId(prefs.getString(KEY_MODE, CalculationMode.TURBO_MAP.id))
        set(value) = prefs.edit().putString(KEY_MODE, value.id).apply()

    var tiBase: Double
        get() = prefs.getFloat(KEY_TI_BASE, 1.55f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_TI_BASE, value.toFloat()).apply()

    var mapBase: Double
        get() = prefs.getFloat(KEY_MAP_BASE, -0.60f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_MAP_BASE, value.toFloat()).apply()

    var maxBoost: Double
        get() = prefs.getFloat(KEY_MAX_BOOST, 1.20f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_MAX_BOOST, value.toFloat()).apply()

    var targetRpm: Int
        get() = prefs.getInt(KEY_TARGET_RPM, 6500)
        set(value) = prefs.edit().putInt(KEY_TARGET_RPM, value).apply()

    var exportFormat: EcuExportFormat
        get() {
            val name = prefs.getString(KEY_EXPORT_FORMAT, EcuExportFormat.TAB_SEPARATED.name)
            return try {
                EcuExportFormat.valueOf(name ?: EcuExportFormat.TAB_SEPARATED.name)
            } catch (e: Exception) {
                EcuExportFormat.TAB_SEPARATED
            }
        }
        set(value) = prefs.edit().putString(KEY_EXPORT_FORMAT, value.name).apply()

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_FUEL = "pref_fuel_type"
        private const val KEY_INJECTOR = "pref_injector_range"
        private const val KEY_MODE = "pref_calculation_mode"
        private const val KEY_TI_BASE = "pref_ti_base"
        private const val KEY_MAP_BASE = "pref_map_base"
        private const val KEY_MAX_BOOST = "pref_max_boost"
        private const val KEY_TARGET_RPM = "pref_target_rpm"
        private const val KEY_EXPORT_FORMAT = "pref_export_format"
    }
}
