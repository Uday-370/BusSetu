package com.example.bussetu.core.navigation

// --- EXACT IMPORTS BASED ON YOUR SCREENSHOTS ---
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bussetu.core.welcome_screen.WelcomeScreen
import com.example.bussetu.feature_auth.presentation.LoginScreen
import com.example.bussetu.feature_dashboard.presentation.userdashboard.UserDashboardScreen
import com.example.bussetu.feature_driver.presentation.DriverDashboardScreen
import com.example.bussetu.feature_chatbot.presentation.ChatbotScreen
import com.example.bussetu.feature_map.presentation.mapscreen.MapScreen
import android.net.Uri

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
            val context = LocalContext.current

            DriverDashboardScreen(
                onBackClick = {
                    (context as? Activity)?.moveTaskToBack(true)
                },
                onLogoutClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 4. User Dashboard
        composable(route = Screen.UserDashboard.route) {
            UserDashboardScreen(
                onMenuClick = { /* Drawer */ },
                onNavigateToMap = { tripId ->
                    navController.navigate(Screen.Map.createRoute(tripId))
                },
                onNavigateToChatbot = {
                    navController.navigate(Screen.Chatbot.route)
                }
            )
        }

        // 6. Chatbot Screen
        composable(route = Screen.Chatbot.route) {
            ChatbotScreen(onBackClick = { navController.popBackStack() })
        }

        // 5. Map Screen
        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.IntType } // ✅ Expects Int
            )
        ) { backStackEntry ->
            // Extract the Int!
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: -1

            MapScreen(
                onBackClick = { navController.popBackStack() },
                onChatbotClick = { navController.navigate(Screen.Chatbot.route) }
            )
        }
    }
}