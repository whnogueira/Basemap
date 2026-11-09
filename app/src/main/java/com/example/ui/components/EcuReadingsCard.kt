package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculationMode
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import java.util.Locale
import kotlin.math.round

@Composable
fun EcuReadingsCard(
    mode: CalculationMode,
    tiBase: Double,
    mapBase: Double,
    onTiBaseChanged: (Double) -> Unit,
    onMapBaseChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingTiDialog by remember { mutableStateOf(false) }
    var editingMapDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ecu_readings_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AvTimer,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INFORMAÇÕES LIDAS NA ECU",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }

                val modeBadgeText = when (mode) {
                    CalculationMode.TURBO_MAP -> "MARCHA LENTA"
                    CalculationMode.ASPIRADO_MAP -> "VÁCUO DO COLETOR"
                    CalculationMode.ASPIRADO_TPS -> "BORBOLETA / ITB"
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AmberAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = modeBadgeText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Tempo de Injeção
            val tiLabel = when (mode) {
                CalculationMode.ASPIRADO_TPS -> "Tempo de Injeção na Marcha Lenta (0% TPS)"
                CalculationMode.ASPIRADO_MAP -> "Tempo de Injeção na Marcha Lenta"
                CalculationMode.TURBO_MAP -> "Tempo de Injeção"
            }

            EcuSliderControl(
                label = tiLabel,
                unit = "ms",
                value = tiBase,
                minValue = 0.80f,
                maxValue = 4.00f,
                step = 0.05,
                onValueChange = { onTiBaseChanged(round(it * 100.0) / 100.0) },
                onValueStep = { delta ->
                    val next = (tiBase + delta).coerceIn(0.80, 4.00)
                    onTiBaseChanged(round(next * 100.0) / 100.0)
                },
                onEditClick = { editingTiDialog = true },
                testTagPrefix = "ti"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Load Reference Control: Depends on mode
            when (mode) {
                CalculationMode.ASPIRADO_TPS -> {
                    // For TPS mode: show throttle baseline info & calibration indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Referência de Carga TPS:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "0% a 100% TPS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No modo Aspirado TPS, a injeção parte do tempo na lenta (0% TPS) e segue a área de vazão das borboletas até 100% (WOT). Ideal para corpos individuais (ITB) ou comando bravo com vácuo instável.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                CalculationMode.ASPIRADO_MAP -> {
                    // For Aspirado MAP mode: vacuum pressure slider with camshaft indication
                    EcuSliderControl(
                        label = "Vácuo do MAP na Marcha Lenta",
                        unit = "bar",
                        value = mapBase,
                        minValue = -1.00f,
                        maxValue = 0.00f,
                        step = 0.05,
                        onValueChange = { onMapBaseChanged(round(it * 100.0) / 100.0) },
                        onValueStep = { delta ->
                            val next = (mapBase + delta).coerceIn(-1.00, 0.00)
                            onMapBaseChanged(round(next * 100.0) / 100.0)
                        },
                        onEditClick = { editingMapDialog = true },
                        testTagPrefix = "map"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val camIndication = when {
                        mapBase <= -0.60 -> "Comando Original / Rua (Vácuo Estável: ~-0,65 a -0,60 bar)"
                        mapBase <= -0.45 -> "Comando Graduado / Esportivo (272° a 288°: ~-0,55 a -0,45 bar)"
                        else -> "Comando Extremo / Arrancada (296°+ Overlap Alto: ~-0,40 a -0,30 bar)"
                    }

                    Text(
                        text = "• $camIndication",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary.copy(alpha = 0.85f)
                    )
                }
                CalculationMode.TURBO_MAP -> {
                    // Standard Turbo MAP control
                    EcuSliderControl(
                        label = "Valor do MAP",
                        unit = "bar",
                        value = mapBase,
                        minValue = -1.00f,
                        maxValue = 0.00f,
                        step = 0.05,
                        onValueChange = { onMapBaseChanged(round(it * 100.0) / 100.0) },
                        onValueStep = { delta ->
                            val next = (mapBase + delta).coerceIn(-1.00, 0.00)
                            onMapBaseChanged(round(next * 100.0) / 100.0)
                        },
                        onEditClick = { editingMapDialog = true },
                        testTagPrefix = "map"
                    )
                }
            }
        }
    }

    if (editingTiDialog) {
        NumericInputDialog(
            title = "Ajustar Tempo de Injeção",
            unit = "ms",
            currentValue = tiBase,
            minValue = 0.50,
            maxValue = 8.00,
            onDismiss = { editingTiDialog = false },
            onConfirm = {
                onTiBaseChanged(it)
                editingTiDialog = false
            }
        )
    }

    if (editingMapDialog) {
        NumericInputDialog(
            title = "Ajustar Vácuo do MAP (Lenta)",
            unit = "bar",
            currentValue = mapBase,
            minValue = -1.00,
            maxValue = 0.00,
            onDismiss = { editingMapDialog = false },
            onConfirm = {
                onMapBaseChanged(it)
                editingMapDialog = false
            }
        )
    }
}

@Composable
private fun EcuSliderControl(
    label: String,
    unit: String,
    value: Double,
    minValue: Float,
    maxValue: Float,
    step: Double,
    onValueChange: (Double) -> Unit,
    onValueStep: (Double) -> Unit,
    onEditClick: () -> Unit,
    testTagPrefix: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Value Display Pill with tap-to-edit
            Surface(
                modifier = Modifier
                    .clickable(onClick = onEditClick)
                    .testTag("${testTagPrefix}_display_pill"),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f", value).replace('.', ','),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar valor",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onValueStep(-step) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_minus_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuir $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toDouble()) },
                valueRange = minValue..maxValue,
                colors = SliderDefaults.colors(
                    thumbColor = CyanPrimary,
                    activeTrackColor = CyanPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("${testTagPrefix}_slider")
            )

            IconButton(
                onClick = { onValueStep(step) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_plus_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NumericInputDialog(
    title: String,
    unit: String,
    currentValue: Double,
    minValue: Double,
    maxValue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var textValue by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", currentValue))
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                Text(
                    text = "Digite o valor lido na ECU ($minValue a $maxValue $unit):",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = false
                    },
                    isError = isError,
                    singleLine = true,
                    suffix = { Text(unit) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Valor inválido. Digite um número válido.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = textValue.replace(',', '.').toDoubleOrNull()
                    if (parsed != null && parsed >= minValue && parsed <= maxValue) {
                        onConfirm(round(parsed * 100.0) / 100.0)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
