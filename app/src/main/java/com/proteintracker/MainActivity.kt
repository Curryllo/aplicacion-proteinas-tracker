package com.proteintracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.proteintracker.ui.ProteinTrackerRoot
import com.proteintracker.ui.theme.ProteinTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ProteinTrackerTheme {
                ProteinTrackerRoot()
            }
        }
    }
}
