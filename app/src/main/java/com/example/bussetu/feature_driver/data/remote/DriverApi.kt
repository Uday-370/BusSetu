package com.example.bussetu.feature_driver.data.remote

import com.example.bussetu.feature_driver.domain.model.Bus
import com.example.bussetu.feature_driver.domain.model.Route
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class EndTripRequest(
    val trip_id: Int
)

data class EndTripResponse(
    val message: String
)

// What we send to the server when "Start Duty" is clicked
data class StartTripRequest(
    val bus_id: Int,
    val route_id: Int,
    val driver_id: Int
)

data class UpdateLocationRequest(
    val trip_id: Int,
    val latitude: Double,
    val longitude: Double
)

// What the server returns (the new trip ID)
data class StartTripResponse(
    val trip_id: Int,
    val message: String
)

data class UpdateLocationResponse(
    val success: Boolean,
    val message: String
)

interface DriverApi {

    // 1. Get list of available buses for the dropdown
    @GET("api/driver/buses") // Adjust to your actual backend URL
    suspend fun getAvailableBuses(): List<Bus>

    // 2. Get list of routes for the dropdown
    @GET("api/driver/routes")
    suspend fun getRoutes(): List<Route>

    // 3. Create a new trip in the 'trips' table
    @POST("api/driver/trip/start")
    suspend fun startTrip(
        @Body request: StartTripRequest
    ): StartTripResponse

    @POST("api/driver/trip/end")
    suspend fun endTrip(
        @Body request: EndTripRequest
    ): EndTripResponse

    @POST("api/driver/location/update")
    suspend fun updateLocation(
        @Body request: UpdateLocationRequest
    ): UpdateLocationResponse
}