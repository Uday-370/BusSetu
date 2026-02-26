package com.example.bussetu.core.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// --- EXACT IMPORTS BASED ON YOUR SCREENSHOTS ---

// Welcome Screen is in core -> presentation -> welcome_screen
import com.example.bussetu.core.welcome_screen.WelcomeScreen

// Login Screen is in feature_auth.presentation
import com.example.bussetu.feature_auth.presentation.LoginScreen

// Driver Dashboard is in feature_driver.presentation
import com.example.bussetu.feature_driver.presentation.DriverDashboardScreen

// User Dashboard is in feature_map.presentation -> userdashboard
import com.example.bussetu.feature_map.presentation.userdashboard.UserDashboardScreen

// Map Screen is in feature_map.presentation -> mapscreen
import com.example.bussetu.feature_map.presentation.mapscreen.MapScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun BusSetuNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Welcome Screen
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onDriverClick = {
                    navController.navigate(Screen.Login.route)
                },
                onUserClick = {
                    navController.navigate(Screen.UserDashboard.route)
                }
            )
        }

        // 2. Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.DriverDashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // 3. Driver Dashboard
        composable(Screen.DriverDashboard.route) {
            val context = LocalContext.current // Get the activity context

            DriverDashboardScreen(
                onBackClick = {
                    // ✅ THE FIX: Standard Android Behavior.
                    // This minimizes the app to the background instead of going to the Welcome screen!
                    (context as? Activity)?.moveTaskToBack(true)
                },
                onLogoutClick = {
                    // ONLY the explicit logout button goes back to the Welcome screen
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 4. User Dashboard (Passenger)
        composable(route = Screen.UserDashboard.route) {
            UserDashboardScreen(
                onMenuClick = { /* Handle menu later */ },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }

        // 5. Map Screen
        composable(route = Screen.Map.route) {
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}