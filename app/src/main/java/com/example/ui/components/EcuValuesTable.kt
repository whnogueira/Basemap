package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculationMode
import com.example.data.model.EcuExportFormat
import com.example.data.model.EcuPoint
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary

@Composable
fun EcuValuesTable(
    points: List<EcuPoint>,
    mode: CalculationMode,
    selectedFormat: EcuExportFormat,
    onFormatChanged: (EcuExportFormat) -> Unit,
    onCopyValues: () -> Unit,
    onShareValues: () -> Unit,
    copiedState: Boolean,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    var formatDropdownExpanded by remember { mutableStateOf(false) }
    val maxTime = points.maxOfOrNull { it.injectionTimeMs } ?: 10.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ecu_values_table_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TABELA DE CALIBRAÇÃO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                }

                // Format selector pill
                Box {
                    Surface(
                        modifier = Modifier
                            .clickable { formatDropdownExpanded = true }
                            .testTag("export_format_button"),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = selectedFormat.displayName.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = formatDropdownExpanded,
                        onDismissRequest = { formatDropdownExpanded = false }
                    ) {
                        EcuExportFormat.entries.forEach { format ->
                            DropdownMenuItem(
                                text = { Text(format.displayName, fontSize = 13.sp) },
                                onClick = {
                                    onFormatChanged(format)
                                    formatDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Copiar & Compartilhar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopyValues,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("copy_table_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (copiedState) Color(0xFF00E676) else CyanPrimary,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(
                        imageVector = if (copiedState) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copiedState) "VALORES COPIADOS!" else "COPIAR VALORES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                OutlinedButton(
                    onClick = onShareValues,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp)
                        .testTag("share_table_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COMPARTILHAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Table Header
            val loadHeader = if (mode == CalculationMode.ASPIRADO_TPS) "TPS (%)" else "MAP (bar)"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = Color(0xFF0B132B),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loadHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Tempo (ms)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Duty Cycle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Table Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(
                        BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            ) {
                points.forEachIndexed { index, point ->
                    val isEven = index % 2 == 0
                    val rowBg = if (point.isAtmospheric) {
                        AmberAccent.copy(alpha = 0.12f)
                    } else if (point.isReference) {
                        CyanPrimary.copy(alpha = 0.12f)
                    } else if (isEven) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    } else {
                        Color(0xFF090D18)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Load / MAP Column
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${point.formattedLoad} ${mode.unit}",
                                fontSize = 12.sp,
                                fontWeight = if (point.isReference || point.isAtmospheric) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                color = if (point.isAtmospheric) AmberAccent else if (point.isReference) CyanPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (point.stageTag != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                val tagColor = when (point.stageTag) {
                                    "LENTA" -> CyanPrimary
                                    "CRUZ." -> AmberAccent
                                    "ATMO" -> AmberAccent
                                    "WOT", "PICO" -> Color(0xFFFF5252)
                                    else -> CyanPrimary
                                }
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = tagColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = point.stageTag,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = tagColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Injection Time with tiny pulse-width bar
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${point.formattedTime} ms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color.DarkGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = (point.injectionTimeMs / maxTime).toFloat().coerceIn(0.05f, 1f))
                                        .background(CyanPrimary)
                                )
                            }
                        }

                        // Duty Cycle Column
                        val dcColor = when {
                            point.dutyCyclePercent > 85.0 -> Color(0xFFFF1744)
                            point.dutyCyclePercent > 75.0 -> AmberAccent
                            else -> Color(0xFF00E676)
                        }

                        Text(
                            text = "${point.dutyCyclePercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = dcColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Dica: Os valores copiados podem ser colados diretamente no software da sua ECU (InjePro, FuelTech, Megasquirt, TunerStudio).",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
