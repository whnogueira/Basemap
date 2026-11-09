package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EcuPoint
import com.example.domain.BaseMapCalculator
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary

@Composable
fun DutyCycleInspectorDialog(
    targetRpm: Int,
    onRpmChanged: (Int) -> Unit,
    peakPoint: EcuPoint?,
    onDismiss: () -> Unit
) {
    val peakMs = peakPoint?.injectionTimeMs ?: 9.76
    val currentDutyCycle = BaseMapCalculator.calculateDutyCycle(peakMs, targetRpm)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INSPEÇÃO DE DUTY CYCLE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Verifique se a vazão dos injetores suporta o regime máximo de rotação sem travar abertos (ideal < 80-85%).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rotação Máxima (RPM):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$targetRpm RPM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary
                    )
                }

                Slider(
                    value = targetRpm.toFloat(),
                    onValueChange = { onRpmChanged(it.toInt()) },
                    valueRange = 4000f..9500f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                val statusColor = when {
                    currentDutyCycle > 85.0 -> Color(0xFFFF1744)
                    currentDutyCycle > 75.0 -> AmberAccent
                    else -> Color(0xFF00E676)
                }

                val statusText = when {
                    currentDutyCycle > 85.0 -> "PERIGO: Injetores em saturação (>85%)! Risco de travar abertos ou queima de bobina. Recomendado injetores maiores ou suplementares."
                    currentDutyCycle > 75.0 -> "ATENÇÃO: Duty cycle moderado/alto (>75%). Monitore em dias frios / ar denso."
                    else -> "SEGURO: Margem de segurança excelente (<75%)."
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "DUTY NO PICO (${peakPoint?.formattedLoad ?: "1.20"} bar):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentDutyCycle%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = statusColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        }
    )
}
