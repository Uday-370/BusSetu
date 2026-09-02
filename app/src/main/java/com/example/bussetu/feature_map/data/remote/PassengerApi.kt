package com.example.bussetu.feature_map.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PassengerApi {

    // 0. Get all stops to populate the dashboard search suggestions
    @GET("api/stops")
    suspend fun getAllStops(): List<StopDto>

    // 1. Get all active trips so we can filter client-side
    @GET("api/trips/active")
    suspend fun getActiveTrips(): List<ActiveTripDto>

    // 2. Get all the stops for a specific route to draw them on the map
    @GET("api/stops/route/{route_id}")
    suspend fun getRouteStops(
        @Path("route_id") routeId: Int // Notice this is an Int now!
    ): List<StopDto>

    // 3. The 5-second polling loop for the bus's live location
    @GET("api/locations/trip/{trip_id}/latest")
    suspend fun getLiveLocation(
        @Path("trip_id") tripId: Int // Also an Int!
    ): LiveLocationDto?
}