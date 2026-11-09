package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.TunerScreen
import com.example.ui.TunerViewModel
import com.example.ui.theme.MapaBaseTurboTheme

class MainActivity : ComponentActivity() {

    private val tunerViewModel: TunerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapaBaseTurboTheme {
                TunerScreen(viewModel = tunerViewModel)
            }
        }
    }
}
