package com.example.bussetu.feature_map.domain.model
import org.osmdroid.util.GeoPoint

data class BusRouteStop(
    val id: String,
    val name: String,
    val scheduledTime: String,
    val delayMinutes: Int,
    val status: StopStatus,
    val location: GeoPoint
)