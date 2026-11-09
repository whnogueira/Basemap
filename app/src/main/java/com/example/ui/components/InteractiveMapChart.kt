package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import com.example.ui.theme.TextSecondary
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculationMode
import com.example.data.model.EcuPoint
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GaugeGreen
import com.example.ui.theme.GaugeOrange
import com.example.ui.theme.GaugeRed
import com.example.ui.theme.GaugeYellow
import kotlin.math.max

@Composable
fun InteractiveMapChart(
    points: List<EcuPoint>,
    mode: CalculationMode,
    tiBase: Double,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    var selectedPoint by remember { mutableStateOf<EcuPoint?>(null) }
    val maxTime = (points.maxOfOrNull { it.injectionTimeMs } ?: 10.0) * 1.10
    val minTime = points.minOfOrNull { it.injectionTimeMs } ?: tiBase

    val idlePoint = points.find { it.isReference } ?: points.firstOrNull()
    val atmoPoint = points.find { it.isAtmospheric } ?: points.getOrNull(points.size / 2)
    val peakPoint = points.find { it.isPeak } ?: points.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_map_chart_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with title and side callout badges
            val chartHeaderTitle = when (mode) {
                CalculationMode.TURBO_MAP -> "Turbo por MAP"
                CalculationMode.ASPIRADO_MAP -> "Aspirado por MAP (Vácuo)"
                CalculationMode.ASPIRADO_TPS -> "Aspirado por TPS (Borboleta)"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = chartHeaderTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Tempo de Injeção (ms)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted Three Key Points adapted to Mode
            val (card1Title, card2Title, card3Title) = when (mode) {
                CalculationMode.TURBO_MAP -> Triple("LENTA", "ATMOSFÉRICO", "PICO BOOST")
                CalculationMode.ASPIRADO_MAP -> Triple("LENTA", "CRUZEIRO", "PLENA CARGA (WOT)")
                CalculationMode.ASPIRADO_TPS -> Triple("MARCHA LENTA", "CRUZEIRO", "PLENA CARGA (WOT)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (idlePoint != null) {
                    SideCalloutCard(
                        title = card1Title,
                        subtitle = "${idlePoint.formattedLoad} ${mode.unit}",
                        value = "${idlePoint.formattedTime} ms",
                        accentColor = GaugeGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (atmoPoint != null) {
                    SideCalloutCard(
                        title = card2Title,
                        subtitle = "${atmoPoint.formattedLoad} ${mode.unit}",
                        value = "${atmoPoint.formattedTime} ms",
                        accentColor = GaugeYellow,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (peakPoint != null) {
                    SideCalloutCard(
                        title = card3Title,
                        subtitle = "${peakPoint.formattedLoad} ${mode.unit}",
                        value = "${peakPoint.formattedTime} ms",
                        accentColor = if (mode == CalculationMode.ASPIRADO_MAP) GaugeOrange else GaugeRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart area
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF090D18),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    points.forEach { point ->
                        val isCurrentSelected = selectedPoint == point
                        BarColumn(
                            point = point,
                            maxTime = maxTime,
                            minTime = minTime,
                            isSelected = isCurrentSelected,
                            onClick = {
                                selectedPoint = if (selectedPoint == point) null else point
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Axis Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pressão / Carga (${mode.unit})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Toque na barra para inspecionar",
                    fontSize = 11.sp,
                    color = CyanPrimary.copy(alpha = 0.8f)
                )
            }

            // Inspection banner when a bar is selected
            if (selectedPoint != null) {
                val p = selectedPoint!!
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PONTO SELECIONADO: ${p.formattedLoad} ${mode.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Tempo: ${p.formattedTime} ms • Duty Cycle estimado: ${p.dutyCyclePercent}%",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = getBarColor(p.injectionTimeMs, minTime, maxTime)
                        ) {
                            Text(
                                text = "${p.formattedTime} ms",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarColumn(
    point: EcuPoint,
    maxTime: Double,
    minTime: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val barHeightRatio = (point.injectionTimeMs / maxTime).coerceIn(0.08, 1.0)
    val barColor = getBarColor(point.injectionTimeMs, minTime, maxTime)

    val animatedHeightRatio by animateFloatAsState(
        targetValue = barHeightRatio.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "barHeight"
    )

    Column(
        modifier = Modifier
            .width(42.dp)
            .height(240.dp)
            .clickable(onClick = onClick)
            .testTag("bar_${point.displayLabel}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Injection time label on top of bar
        Text(
            text = point.formattedTime,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // The Bar itself
        Box(
            modifier = Modifier
                .width(if (isSelected) 34.dp else 28.dp)
                .fillMaxHeight(fraction = animatedHeightRatio.coerceAtLeast(0.05f))
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            barColor,
                            barColor.copy(alpha = 0.85f)
                        )
                    )
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            BorderStroke(1.5.dp, Color.White),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                    } else if (point.isAtmospheric) {
                        Modifier.border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                    } else Modifier
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // MAP label under bar
        Text(
            text = point.formattedLoad,
            fontSize = 9.sp,
            fontWeight = if (point.isReference || point.isAtmospheric || isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = if (point.isAtmospheric) AmberAccent else if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SideCalloutCard(
    title: String,
    subtitle: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }
    }
}

/**
 * Maps injection time value into a smooth motorsport gradient:
 * Green -> Yellow -> Orange -> Red
 */
private fun getBarColor(value: Double, minVal: Double, maxVal: Double): Color {
    val progress = ((value - minVal) / max(0.1, maxVal - minVal)).coerceIn(0.0, 1.0)
    return when {
        progress < 0.33 -> {
            val local = progress / 0.33
            interpolateColor(GaugeGreen, GaugeYellow, local.toFloat())
        }
        progress < 0.66 -> {
            val local = (progress - 0.33) / 0.33
            interpolateColor(GaugeYellow, GaugeOrange, local.toFloat())
        }
        else -> {
            val local = (progress - 0.66) / 0.34
            interpolateColor(GaugeOrange, GaugeRed, local.toFloat())
        }
    }
}

private fun interpolateColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = 1.0f
    )
}
