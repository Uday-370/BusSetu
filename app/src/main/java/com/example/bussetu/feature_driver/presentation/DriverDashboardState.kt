package com.example.bussetu.feature_driver.presentation

import com.example.bussetu.feature_driver.domain.model.Bus
import com.example.bussetu.feature_driver.domain.model.Route

data class DriverDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Data for the dropdowns
    val availableBuses: List<Bus> = emptyList(),
    val availableRoutes: List<Route> = emptyList(),

    // User selections
    val selectedBus: Bus? = null,
    val selectedRoute: Route? = null,

    // Is the driver currently driving?
    val isTracking: Boolean = false,
    val currentTripId: Int? = null
)