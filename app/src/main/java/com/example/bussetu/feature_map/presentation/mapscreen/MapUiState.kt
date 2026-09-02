package com.example.bussetu.feature_map.presentation.mapscreen

import com.example.bussetu.feature_map.domain.model.BusRouteStop
import org.osmdroid.util.GeoPoint

// This represents everything the MapScreen needs to draw itself perfectly.
data class MapUiState(
    val routeCode: String = "", // ✅ The dynamic search query!
    val destinationTitle: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val routePoints: List<GeoPoint> = emptyList(),
    val stops: List<BusRouteStop> = emptyList(),
    val currentBusLocation: GeoPoint? = null,
    val isDelayed: Boolean = false,
    val currentDelayMinutes: Int = 0
)