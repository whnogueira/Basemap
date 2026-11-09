package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CalculationMode
import com.example.data.model.EcuExportFormat
import com.example.data.model.EcuPoint
import com.example.data.model.FuelType
import com.example.data.model.InjectorRange
import com.example.data.model.TunerPreset
import com.example.data.repository.TunerPreferencesRepository
import com.example.domain.BaseMapCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class BaseParams(
    val fuel: FuelType,
    val inj: InjectorRange,
    val ti: Double,
    val map: Double
)

private data class TargetParams(
    val mode: CalculationMode,
    val boost: Double,
    val rpm: Int
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TunerPreferencesRepository(application)

    private val _fuelType = MutableStateFlow(repository.fuelType)
    val fuelType: StateFlow<FuelType> = _fuelType.asStateFlow()

    private val _injectorRange = MutableStateFlow(repository.injectorRange)
    val injectorRange: StateFlow<InjectorRange> = _injectorRange.asStateFlow()

    private val _tiBase = MutableStateFlow(repository.tiBase)
    val tiBase: StateFlow<Double> = _tiBase.asStateFlow()

    private val _mapBase = MutableStateFlow(repository.mapBase)
    val mapBase: StateFlow<Double> = _mapBase.asStateFlow()

    private val _mode = MutableStateFlow(repository.calculationMode)
    val mode: StateFlow<CalculationMode> = _mode.asStateFlow()

    private val _maxBoost = MutableStateFlow(repository.maxBoost)
    val maxBoost: StateFlow<Double> = _maxBoost.asStateFlow()

    private val _targetRpm = MutableStateFlow(repository.targetRpm)
    val targetRpm: StateFlow<Int> = _targetRpm.asStateFlow()

    private val _exportFormat = MutableStateFlow(repository.exportFormat)
    val exportFormat: StateFlow<EcuExportFormat> = _exportFormat.asStateFlow()

    private val _copiedState = MutableStateFlow(false)
    val copiedState: StateFlow<Boolean> = _copiedState.asStateFlow()

    private val baseParamsFlow = combine(_fuelType, _injectorRange, _tiBase, _mapBase) { fuel, inj, ti, map ->
        BaseParams(fuel, inj, ti, map)
    }

    private val targetParamsFlow = combine(_mode, _maxBoost, _targetRpm) { mode, boost, rpm ->
        TargetParams(mode, boost, rpm)
    }

    val points: StateFlow<List<EcuPoint>> = combine(baseParamsFlow, targetParamsFlow) { base, target ->
        BaseMapCalculator.calculate(
            fuelType = base.fuel,
            injectorRange = base.inj,
            tiBase = base.ti,
            mapBase = base.map,
            mode = target.mode,
            maxBoost = target.boost,
            targetRpm = target.rpm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFuelType(fuel: FuelType) {
        _fuelType.value = fuel
        repository.fuelType = fuel
    }

    fun setInjectorRange(inj: InjectorRange) {
        _injectorRange.value = inj
        repository.injectorRange = inj
    }

    fun setTiBase(ti: Double) {
        _tiBase.value = ti
        repository.tiBase = ti
    }

    fun setMapBase(map: Double) {
        _mapBase.value = map
        repository.mapBase = map
    }

    fun setMode(mode: CalculationMode) {
        _mode.value = mode
        repository.calculationMode = mode
    }

    fun setMaxBoost(boost: Double) {
        _maxBoost.value = boost
        repository.maxBoost = boost
    }

    fun setTargetRpm(rpm: Int) {
        _targetRpm.value = rpm
        repository.targetRpm = rpm
    }

    fun setExportFormat(format: EcuExportFormat) {
        _exportFormat.value = format
        repository.exportFormat = format
    }

    fun applyPreset(preset: TunerPreset) {
        _fuelType.value = preset.fuelType
        repository.fuelType = preset.fuelType

        _injectorRange.value = preset.injectorRange
        repository.injectorRange = preset.injectorRange

        _tiBase.value = preset.tiBase
        repository.tiBase = preset.tiBase

        _mapBase.value = preset.mapBase
        repository.mapBase = preset.mapBase

        _mode.value = preset.mode
        repository.calculationMode = preset.mode

        if (preset.maxBoost > 0.0) {
            _maxBoost.value = preset.maxBoost
            repository.maxBoost = preset.maxBoost
        }
    }

    fun resetToDefaults() {
        val defaultPreset = TunerPreset.PRESETS.first()
        applyPreset(defaultPreset)
    }

    fun copyValuesToClipboard(context: Context) {
        val currentPoints = points.value
        if (currentPoints.isEmpty()) return

        val text = exportFormat.value.format(
            points = currentPoints,
            mode = _mode.value,
            fuel = _fuelType.value,
            inj = _injectorRange.value
        )

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mapa Base ECU", text)
        clipboard.setPrimaryClip(clip)

        viewModelScope.launch {
            _copiedState.value = true
            Toast.makeText(context, "Valores copiados no formato ${exportFormat.value.displayName}!", Toast.LENGTH_SHORT).show()
            delay(2500)
            _copiedState.value = false
        }
    }

    fun shareValues(context: Context) {
        val currentPoints = points.value
        if (currentPoints.isEmpty()) return

        val text = exportFormat.value.format(
            points = currentPoints,
            mode = _mode.value,
            fuel = _fuelType.value,
            inj = _injectorRange.value
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Mapa Base ECU - ${_mode.value.title}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Mapa Base")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
