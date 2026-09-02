package com.example.bussetu.feature_driver.data.repository

import com.example.bussetu.feature_driver.data.remote.DriverApi
import com.example.bussetu.feature_driver.data.remote.StartTripRequest
import com.example.bussetu.feature_driver.data.remote.UpdateLocationRequest
import com.example.bussetu.feature_driver.domain.model.Bus
import com.example.bussetu.feature_driver.domain.model.Route
import com.example.bussetu.feature_driver.domain.repository.DriverRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class DriverRepositoryImpl @Inject constructor(
    private val api: DriverApi
) : DriverRepository {

    override suspend fun getAvailableBuses(): Result<List<Bus>> {
        return try {
            val buses = api.getAvailableBuses()
            Result.success(buses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoutes(): Result<List<Route>> {
        return try {
            val routes = api.getRoutes()
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTrip(busId: Int, routeId: Int, driverId: Int): Result<Int> {
        return try {
            val request = StartTripRequest(
                bus_id = busId,
                route_id = routeId
            )
            val response = api.startTrip(request)
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ ADDED: Fake Backend implementation for ending a trip
    override suspend fun endTrip(tripId: Int): Result<Unit> {
        return try {
            api.endTrip(tripId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ ADDED: Fake Backend implementation for location updates
    override suspend fun updateLocation(tripId: Int, latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val request = UpdateLocationRequest(
                trip_id = tripId,
                latitude = latitude,
                longitude = longitude
            )
            api.updateLocation(request)
            // If the API call completes without throwing an exception, it was successful
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}