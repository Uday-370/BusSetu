package com.example.bussetu.feature_map.data.remote

import com.google.gson.annotations.SerializedName

// Matches your 'stops' table (plus the new time_offset we asked for!)
data class StopDto(
    val id: Int,
    val stop_name: String,
    val latitude: Double,
    val longitude: Double,
    val stop_order: Int,
    val time_offset_minutes: Int? // The new column the backend is adding
)

// Represents an active trip from the backend
data class ActiveTripDto(
    val id: Int,
    val driver_id: Int,
    val bus_id: Int,
    val route_id: Int,
    val status: String,
    val bus_number: String,
    val route_name: String,
    val driver_name: String,
    val current_lat: Double?,
    val current_lng: Double?,
    val speed: Double?,
    val last_update: String?
)

// Matches your 'live_locations' table
data class LiveLocationDto(
    val id: Int,
    val trip_id: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Double?,
    val timestamp: String
)