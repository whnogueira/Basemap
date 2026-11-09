package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary

@Composable
fun TunerInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GUIA DO MAPA BASE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Como funciona a lógica de cálculo?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
                Text(
                    text = "O mapa base utiliza o tempo de injeção e o MAP medidos com o motor na marcha lenta aquecido (ex: 1,55 ms @ -0,60 bar).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Por que o tempo é constante no vácuo mais fundo?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
                Text(
                    text = "Abaixo da pressão de marcha lenta (ex: -0,70 a -1,00 bar), o motor opera em desaceleração (freio motor / cut-off). Manter o tempo flat garante que o motor não afogue e facilita a calibração do corte em desaceleração.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Fatores de Combustível",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
                Text(
                    text = "• Gasolina (AFR 14.7:1): Base 1.0x\n• Álcool (AFR 9.0:1): ~1.32x (+32% volume)\n• Metanol (AFR 6.4:1): ~1.85x (+85% volume)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Como usar na sua ECU",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
                Text(
                    text = "1. Toque em 'Copiar Valores' para enviar a curva para a área de transferência.\n2. No software da ECU (InjePro, FuelTech, TunerStudio), abra a tabela principal de Injeção x MAP.\n3. Cole os valores na coluna correspondente.\n4. Dê a partida e use a sonda wideband para acertar o mapa fino em carga real.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        }
    )
}
