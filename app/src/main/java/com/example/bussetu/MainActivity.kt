package com.example.bussetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // ✅ ADDED
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.example.bussetu.core.navigation.BusSetuNavGraph
import com.example.bussetu.core.ui.theme.BusSetuTheme // (Or ui.theme.BusSetuTheme based on your actual path)
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ✅ ADDED: Get the ViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BusSetuTheme {
                // Keep the system bar logic
                val isDarkTheme = isSystemInDarkTheme()
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    val useDarkIcons = !isDarkTheme
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                // ✅ ADDED: Wait for DataStore, then pass the correct Start Destination!
                if (!viewModel.isLoading.value) {
                    BusSetuNavGraph(
                        startDestination = viewModel.startDestination.value
                    )
                }
            }
        }
    }
}