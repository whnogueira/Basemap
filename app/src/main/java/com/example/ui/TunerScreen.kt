package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CalculationModeCard
import com.example.ui.components.DutyCycleInspectorDialog
import com.example.ui.components.EcuReadingsCard
import com.example.ui.components.EcuValuesTable
import com.example.ui.components.FuelSelectorCard
import com.example.ui.components.InjectorSelectorCard
import com.example.ui.components.InteractiveMapChart
import com.example.ui.components.PresetsBottomSheet
import com.example.ui.components.TunerHeroBanner
import com.example.ui.components.TunerInfoDialog
import com.example.ui.theme.CyanPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    viewModel: TunerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val fuelType by viewModel.fuelType.collectAsStateWithLifecycle()
    val injectorRange by viewModel.injectorRange.collectAsStateWithLifecycle()
    val tiBase by viewModel.tiBase.collectAsStateWithLifecycle()
    val mapBase by viewModel.mapBase.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val maxBoost by viewModel.maxBoost.collectAsStateWithLifecycle()
    val targetRpm by viewModel.targetRpm.collectAsStateWithLifecycle()
    val exportFormat by viewModel.exportFormat.collectAsStateWithLifecycle()
    val points by viewModel.points.collectAsStateWithLifecycle()
    val copiedState by viewModel.copiedState.collectAsStateWithLifecycle()

    var showPresetsSheet by remember { mutableStateOf(false) }
    var showDutyDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MAPA BASE TURBO",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPresetsSheet = true },
                        modifier = Modifier.testTag("top_presets_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Presets",
                            tint = CyanPrimary
                        )
                    }
                    IconButton(
                        onClick = { showDutyDialog = true },
                        modifier = Modifier.testTag("top_duty_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Inspecionar Duty Cycle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("top_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Ajuda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.resetToDefaults()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Configurações redefinidas para o padrão!")
                            }
                        },
                        modifier = Modifier.testTag("top_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Redefinir Padrão",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Telemetry Banner
                item {
                    TunerHeroBanner(
                        fuelType = fuelType,
                        injectorRange = injectorRange,
                        mode = mode,
                        tiBase = tiBase,
                        mapBase = mapBase,
                        maxBoost = maxBoost
                    )
                }

                // 2. Input Section: Combustível
                item {
                    FuelSelectorCard(
                        selectedFuel = fuelType,
                        onFuelSelected = { viewModel.setFuelType(it) }
                    )
                }

                // 3. Input Section: Injetores (Lbs/h)
                item {
                    InjectorSelectorCard(
                        selectedInjector = injectorRange,
                        onInjectorSelected = { viewModel.setInjectorRange(it) }
                    )
                }

                // 4. Input Section: Leituras ECU (Tempo de Injeção & Valor do MAP/TPS)
                item {
                    EcuReadingsCard(
                        mode = mode,
                        tiBase = tiBase,
                        mapBase = mapBase,
                        onTiBaseChanged = { viewModel.setTiBase(it) },
                        onMapBaseChanged = { viewModel.setMapBase(it) }
                    )
                }

                // 5. Input Section: Modo & Botão Gerar Mapa
                item {
                    CalculationModeCard(
                        selectedMode = mode,
                        maxBoost = maxBoost,
                        onModeSelected = { viewModel.setMode(it) },
                        onMaxBoostSelected = { viewModel.setMaxBoost(it) },
                        onCalculateClicked = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Mapa gerado com sucesso!")
                            }
                        }
                    )
                }

                // 6. Interactive Bar Chart with Gradient Colors & Side Badges
                item {
                    InteractiveMapChart(
                        points = points,
                        mode = mode,
                        tiBase = tiBase
                    )
                }

                // 7. Data Table & Export Actions
                item {
                    EcuValuesTable(
                        points = points,
                        mode = mode,
                        selectedFormat = exportFormat,
                        onFormatChanged = { viewModel.setExportFormat(it) },
                        onCopyValues = { viewModel.copyValuesToClipboard(context) },
                        onShareValues = { viewModel.shareValues(context) },
                        copiedState = copiedState
                    )
                }
            }
        }
    }

    if (showPresetsSheet) {
        PresetsBottomSheet(
            onPresetSelected = { preset ->
                viewModel.applyPreset(preset)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Preset '${preset.name}' aplicado!")
                }
            },
            onDismiss = { showPresetsSheet = false }
        )
    }

    if (showDutyDialog) {
        DutyCycleInspectorDialog(
            targetRpm = targetRpm,
            onRpmChanged = { viewModel.setTargetRpm(it) },
            peakPoint = points.find { it.isPeak } ?: points.lastOrNull(),
            onDismiss = { showDutyDialog = false }
        )
    }

    if (showInfoDialog) {
        TunerInfoDialog(onDismiss = { showInfoDialog = false })
    }
}
