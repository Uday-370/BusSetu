package com.example.bussetu.feature_driver.data.repository

import com.example.bussetu.feature_driver.data.remote.DriverApi
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
            // FAKE BACKEND: Simulate network delay and return dummy buses
            delay(1000)
            val fakeBuses = listOf(
                Bus(id = 1, busNumber = "MH-13-CU-1234"),
                Bus(id = 2, busNumber = "MH-14-AB-9876"),
                Bus(id = 3, busNumber = "MH-12-XY-5555")
            )
            Result.success(fakeBuses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoutes(): Result<List<Route>> {
        return try {
            // FAKE BACKEND: Simulate network delay and return dummy routes
            delay(1000)
            val fakeRoutes = listOf(
                Route(id = 101, routeName = "City Center -> Airport"),
                Route(id = 102, routeName = "North Station -> Tech Park"),
                Route(id = 103, routeName = "University -> Downtown")
            )
            Result.success(fakeRoutes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTrip(busId: Int, routeId: Int, driverId: Int): Result<Int> {
        return try {
            // FAKE BACKEND: Simulate assigning the trip in PostgreSQL
            delay(1000)
            val fakeTripId = 999
            return Result.success(fakeTripId) // Added 'return' here to stop execution if real code is uncommented below

            /* --- REAL BACKEND CODE (Uncomment when API is ready) ---
            val request = StartTripRequest(
                bus_id = busId,
                route_id = routeId,
                driver_id = driverId
            )
            val response = api.startTrip(request)
            Result.success(response.trip_id)
            -------------------------------------------------------- */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ ADDED: Fake Backend implementation for ending a trip
    override suspend fun endTrip(tripId: Int): Result<Unit> {
        return try {
            // FAKE BACKEND: Simulate network delay for disconnecting
            delay(1500)

            // Return success!
            Result.success(Unit)

            /* --- REAL BACKEND CODE (Uncomment when API is ready) ---
            val response = api.endTrip(EndTripRequest(trip_id = tripId))
            Result.success(Unit)
            -------------------------------------------------------- */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ ADDED: Fake Backend implementation for location updates
    override suspend fun updateLocation(tripId: Int, latitude: Double, longitude: Double): Result<Unit> {
        return try {
            // FAKE BACKEND: We don't need a delay here, just return success so the service keeps humming!
            Result.success(Unit)

            /* --- REAL BACKEND CODE (Uncomment when API is ready) ---
            val request = UpdateLocationRequest(
                trip_id = tripId,
                latitude = latitude,
                longitude = longitude
            )
            api.updateLocation(request)
            Result.success(Unit)
            -------------------------------------------------------- */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}