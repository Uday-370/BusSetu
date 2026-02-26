package com.example.bussetu.feature_driver.domain.repository

import com.example.bussetu.feature_driver.domain.model.Bus
import com.example.bussetu.feature_driver.domain.model.Route

interface DriverRepository {
    suspend fun getAvailableBuses(): Result<List<Bus>>
    suspend fun getRoutes(): Result<List<Route>>
    suspend fun startTrip(busId: Int, routeId: Int, driverId: Int): Result<Int> // Returns the new Trip ID
    suspend fun endTrip(tripId: Int): Result<Unit>
    suspend fun updateLocation(tripId: Int, latitude: Double, longitude: Double): Result<Unit>
}