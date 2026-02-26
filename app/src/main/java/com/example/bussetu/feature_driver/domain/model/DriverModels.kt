package com.example.bussetu.feature_driver.domain.model

// Represents your 'buses' table
data class Bus(
    val id: Int,
    val busNumber: String
)

// Represents your 'routes' table
data class Route(
    val id: Int,
    val routeName: String
)

// The result after clicking "Start Duty" (matches your 'trips' table)
data class ActiveTrip(
    val tripId: Int,
    val busId: Int,
    val routeId: Int,
    val driverId: Int
)