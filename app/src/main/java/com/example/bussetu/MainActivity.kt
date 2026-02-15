package com.example.trackmybus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.example.trackmybus.core.ui.userdashboard.UserDashboardScreen
import com.example.trackmybus.ui.theme.TrackMyBusTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Enable Edge-to-Edge BEFORE super.onCreate
        // This allows the app to draw behind the system bars
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            // Replace 'TrackMyBusTheme' with whatever your main theme wrapper is named
            TrackMyBusTheme {
                // Check if the system is currently in dark mode
                val isDarkTheme = isSystemInDarkTheme()

                // Get the system UI controller (you might need to add the dependency below)
                val systemUiController = rememberSystemUiController()

                // 2. Use a SideEffect to update system bar colors whenever the theme changes
                SideEffect {
                    // If the app background is light (like your white TopBar),
                    // we need dark icons so they are visible.

                    // 'true' creates dark icons (for light backgrounds)
                    // 'false' creates light icons (for dark backgrounds)
                    val useDarkIcons = !isDarkTheme

                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent, // Make the bar itself transparent
                        darkIcons = useDarkIcons // Set icon color contrast
                    )
                }

                // --- Your main app content goes here ---
                // Example: NaveHost or your WelcomeScreen()
                // WelcomeScreen(...)
                UserDashboardScreen(
                    onMenuClick = { /* Handle Menu Click */ },
                    onNavigateToMap = { /* Handle Map Navigation */ }
                )

            }
        }
    }
}

// git add .
// git commit -m "Message"
// git push
//git pull