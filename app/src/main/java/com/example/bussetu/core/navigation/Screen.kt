package com.example.bussetu.core.navigation

// A sealed class ensures we only use these specific routes
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object DriverDashboard : Screen("driver_dashboard_screen")
    object UserDashboard : Screen("user_dashboard_screen")
    object Map : Screen("map_screen")
}