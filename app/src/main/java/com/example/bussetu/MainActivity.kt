package com.example.bussetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
// --- IMPORTS ---
import com.example.bussetu.feature_driver.presentation.DriverDashboardScreen
import com.example.bussetu.feature_auth.presentation.LoginScreen
import com.example.bussetu.feature_map.presentation.mapscreen.MapScreen
import com.example.bussetu.feature_map.presentation.userdashboard.UserDashboardScreen
import com.example.bussetu.core.ui.welcomescreen.WelcomeScreen
import com.example.bussetu.core.ui.theme.BusSetuTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Enable Edge-to-Edge
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BusSetuTheme {
                // --- System Bar Logic ---
                val isDarkTheme = isSystemInDarkTheme()
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    val useDarkIcons = !isDarkTheme
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                // --- 2. NAVIGATION LOGIC ---
                // States: "welcome", "login", "driver_dashboard", "dashboard", "map"
                var currentScreen by remember { mutableStateOf("welcome") }

                when (currentScreen) {
                    "welcome" -> {
                        WelcomeScreen(
                            onDriverClick = {
                                currentScreen = "login"
                            },
                            onUserClick = {
                                currentScreen = "dashboard"
                            }
                        )
                    }

                    "login" -> {
                        // Handle Android Back Button: Go back to Welcome
                        BackHandler { currentScreen = "welcome" }

                        LoginScreen(
                            onLoginClick = {
                                // Navigate to Driver Dashboard upon successful login
                                currentScreen = "driver_dashboard"
                            }
                        )
                    }

                    "driver_dashboard" -> {
                        // Handle Android Back Button: Go back to Welcome (Logout)
                        BackHandler { currentScreen = "welcome" }

                        DriverDashboardScreen(
                            onBackClick = {
                                // The back arrow in your UI goes back to Welcome
                                currentScreen = "welcome"
                            }
                        )
                    }

                    "dashboard" -> {
                        // Handle Android Back Button: Go back to Welcome
                        BackHandler { currentScreen = "welcome" }

                        UserDashboardScreen(
                            onMenuClick = { /* Open Drawer or Menu */ },
                            onNavigateToMap = {
                                currentScreen = "map"
                            }
                        )
                    }

                    "map" -> {
                        // Handle Android Back Button: Go back to Dashboard
                        BackHandler { currentScreen = "dashboard" }

                        MapScreen(
                            onBackClick = { currentScreen = "dashboard" }
                        )
                    }
                }
            }
        }
    }
}