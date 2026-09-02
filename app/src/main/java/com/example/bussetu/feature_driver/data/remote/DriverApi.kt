package com.example.bussetu.feature_driver.data.remote

import com.example.bussetu.feature_driver.domain.model.Bus
import com.example.bussetu.feature_driver.domain.model.Route
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class EndTripRequest(
    val trip_id: Int
)

data class EndTripResponse(
    val message: String
)

// What we send to the server when "Start Duty" is clicked
data class StartTripRequest(
    val bus_id: Int,
    val route_id: Int
)

data class UpdateLocationRequest(
    val trip_id: Int,
    val latitude: Double,
    val longitude: Double
)

// What the server returns (the new trip ID)
data class StartTripResponse(
    val id: Int, // The backend trips table returns the ID as 'id', not 'trip_id'
    val bus_id: Int,
    val route_id: Int,
    val driver_id: Int,
    val status: String
)

// The backend returns the inserted location row, not {success, message}
data class UpdateLocationResponse(
    val id: Int,
    val trip_id: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val timestamp: String
)

interface DriverApi {

    // 1. Get list of available buses for the dropdown
    @GET("api/buses") // Actual backend URL
    suspend fun getAvailableBuses(): List<Bus>

    // 2. Get list of routes for the dropdown
    @GET("api/routes")
    suspend fun getRoutes(): List<Route>

    // 3. Create a new trip in the 'trips' table
    @POST("api/trips/start")
    suspend fun startTrip(
        @Body request: StartTripRequest
    ): StartTripResponse

    @PUT("api/trips/{id}/end")
    suspend fun endTrip(
        @Path("id") tripId: Int
    ): EndTripResponse

    @POST("api/locations/update")
    suspend fun updateLocation(
        @Body request: UpdateLocationRequest
    ): UpdateLocationResponse
}