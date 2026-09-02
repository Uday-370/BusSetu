package com.example.bussetu.feature_map.domain.model

import org.osmdroid.util.GeoPoint

data class LiveBusUpdate(
    val location: GeoPoint,
    val delayMinutes: Int,
    val activeStopId: String,
    val etas: Map<String, Int?>? = null
)