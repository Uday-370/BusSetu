package com.example.bussetu.feature_map.domain.repository

import com.example.bussetu.feature_map.domain.model.BusRouteStop
import com.example.bussetu.feature_map.domain.model.LiveBusUpdate
import org.osmdroid.util.GeoPoint

interface PassengerRepository {
    // 1. The new Search Functions (returns the trip_id as an Int)
    suspend fun getAllStopNames(): Result<List<String>>
    suspend fun searchActiveTripByStops(startStop: String, endStop: String): Result<Int>
    suspend fun searchActiveTripByBusNumber(busNumber: String): Result<Int>

    // 2. The Map Functions (now explicitly taking tripId as an Int!)
    suspend fun getRouteDetails(tripId: Int): Result<Pair<List<GeoPoint>, List<BusRouteStop>>>
    fun getLiveBusLocationFlow(tripId: Int): kotlinx.coroutines.flow.Flow<LiveBusUpdate>
}