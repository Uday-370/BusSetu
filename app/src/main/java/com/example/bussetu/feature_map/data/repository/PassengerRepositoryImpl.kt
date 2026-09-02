package com.example.bussetu.feature_map.data.repository

import com.example.bussetu.feature_map.data.remote.PassengerApi
import com.example.bussetu.feature_map.domain.model.BusRouteStop
import com.example.bussetu.feature_map.domain.model.LiveBusUpdate
import com.example.bussetu.feature_map.domain.model.StopStatus
import com.example.bussetu.feature_map.domain.repository.PassengerRepository
import com.example.bussetu.core.presentation.components.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import io.socket.client.IO
import io.socket.client.Socket
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class PassengerRepositoryImpl @Inject constructor(
    private val api: PassengerApi
) : PassengerRepository {

    // These are real coordinates in Pune.
    private val mockStops = listOf(
        BusRouteStop("1", "Central Station", "10:00 AM", 0, StopStatus.COMPLETED, GeoPoint(18.5204, 73.8567)),
        BusRouteStop("2", "Law College Rd", "10:15 AM", 0, StopStatus.COMPLETED, GeoPoint(18.5225, 73.8580)),
        BusRouteStop("3", "City Park Plaza", "10:30 AM", 15, StopStatus.CURRENT, GeoPoint(18.5245, 73.8600)),
        BusRouteStop("4", "Anno Stop", "10:45 AM", 15, StopStatus.UPCOMING, GeoPoint(18.5270, 73.8625)),
        BusRouteStop("5", "Airport Terminal", "11:00 AM", 15, StopStatus.UPCOMING, GeoPoint(18.5300, 73.8650))
    )

    // We will store the generated road path here so the bus can drive along it
    private var cachedRoadPath: List<GeoPoint> = emptyList()
    private var simulatedIndex = 0

    override suspend fun getAllStopNames(): Result<List<String>> {
        return try {
            val stops = api.getAllStops()
            // Extract unique stop names
            val uniqueNames = stops.map { it.stop_name }.distinct()
            Result.success(uniqueNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchActiveTripByStops(startStop: String, endStop: String): Result<Int> {
        return try {
            val trips = api.getActiveTrips()
            if (trips.isEmpty()) return Result.failure(Exception("No active trips found"))

            // For each active trip, fetch its route stops and check if both start and end
            // stop names are present in that route. This is the correct approach since
            // route_name alone doesn't contain individual stop names.
            for (trip in trips) {
                try {
                    val stops = api.getRouteStops(trip.route_id)
                    val stopNames = stops.map { it.stop_name.lowercase() }
                    val hasStart = stopNames.any { it.contains(startStop.lowercase()) }
                    val hasEnd   = stopNames.any { it.contains(endStop.lowercase()) }
                    if (hasStart && hasEnd) {
                        return Result.success(trip.id)
                    }
                } catch (_: Exception) {
                    // If fetching stops for one trip fails, try the next
                }
            }

            // Fallback: partial match on route_name (legacy routes without stops)
            val fallback = trips.find { trip ->
                trip.route_name.contains(startStop, ignoreCase = true) ||
                trip.route_name.contains(endStop, ignoreCase = true)
            }
            if (fallback != null) {
                Result.success(fallback.id)
            } else {
                Result.failure(Exception("No active bus found for this route. Make sure a driver has started a trip on this route."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchActiveTripByBusNumber(busNumber: String): Result<Int> {
        return try {
            val trips = api.getActiveTrips()
            val matchingTrip = trips.find { it.bus_number.equals(busNumber, ignoreCase = true) }
            
            if (matchingTrip != null) {
                Result.success(matchingTrip.id)
            } else {
                Result.failure(Exception("No active trip found for bus $busNumber"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRouteDetails(tripId: Int): Result<Pair<List<GeoPoint>, List<BusRouteStop>>> {
        return try {
            // First, get the active trips to find the route_id for this trip
            val activeTrips = api.getActiveTrips()
            val trip = activeTrips.find { it.id == tripId }
                ?: throw Exception("Trip not found")

            // Now get the stops for this route
            val stopDtos = api.getRouteStops(trip.route_id)
            
            // Map the DTOs to the Domain model
            val mappedStops = stopDtos.map { dto ->
                BusRouteStop(
                    id = dto.id.toString(),
                    name = dto.stop_name,
                    scheduledTime = "Expected: ${dto.time_offset_minutes ?: 0} mins", // Changed from arrivalTime
                    delayMinutes = 0,
                    status = StopStatus.UPCOMING,
                    location = GeoPoint(dto.latitude, dto.longitude)
                )
            }

            // Extract just the GeoPoints from stops
            val stopPoints = mappedStops.map { it.location }

            // Ask OSRM to calculate the actual road geometry between these stops
            cachedRoadPath = fetchRoadPathFromOSRM(stopPoints)

            Result.success(Pair(cachedRoadPath, mappedStops))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLiveBusLocationFlow(tripId: Int): Flow<LiveBusUpdate> = callbackFlow {
        var socket: Socket? = null
        try {
            // Use the same base URL as Retrofit so the socket works on both
            // emulator (10.0.2.2) and real devices (ngrok/LAN URL).
            val serverUrl = AppConstants.BASE_URL.trimEnd('/')
            socket = IO.socket(serverUrl)
            socket.connect()

            socket.on(Socket.EVENT_CONNECT) {
                // Join the specific trip room
                socket.emit("join_trip", tripId)
            }

            socket.on("location_update") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val lat = data.optDouble("latitude")
                    val lng = data.optDouble("longitude")
                    
                    val etasJson = data.optJSONObject("etas")
                    val etasMap = mutableMapOf<String, Int?>()
                    if (etasJson != null) {
                        val keys = etasJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next() as String
                            if (etasJson.isNull(key)) {
                                etasMap[key] = null
                            } else {
                                etasMap[key] = etasJson.optInt(key)
                            }
                        }
                    }

                    val update = LiveBusUpdate(
                        location = GeoPoint(lat, lng),
                        delayMinutes = 0,
                        activeStopId = "",
                        // Pass null if etas is empty so the ViewModel falls back to Haversine
                        etas = if (etasMap.isEmpty()) null else etasMap
                    )
                    trySend(update)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close(e)
        }

        awaitClose {
            socket?.disconnect()
        }
    }

    // =========================================================================
    // ✅ THE OSRM ROUTING ENGINE
    // =========================================================================
    private suspend fun fetchRoadPathFromOSRM(stops: List<GeoPoint>): List<GeoPoint> = withContext(Dispatchers.IO) {
        try {
            // 1. OSRM requires coordinates in Longitude,Latitude format separated by semicolons
            val coordsString = stops.joinToString(";") { "${it.longitude},${it.latitude}" }

            // 2. We ask for "geojson" format because it's super easy to parse in Kotlin
            val urlString = "https://router.project-osrm.org/route/v1/driving/$coordsString?overview=full&geometries=geojson"
            val url = URL(urlString)

            // 3. Open connection and fetch the data
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "BusSetuAndroidApp/1.0") // ✅ REQUIRED by OSRM

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                // 4. Parse the JSON to extract the hundreds of coordinates that make up the road
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() > 0) {
                    val geometry = routes.getJSONObject(0).getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")

                    val roadPoints = mutableListOf<GeoPoint>()
                    for (i in 0 until coordinates.length()) {
                        val point = coordinates.getJSONArray(i)
                        val lon = point.getDouble(0) // GeoJSON is [Lon, Lat]
                        val lat = point.getDouble(1)
                        roadPoints.add(GeoPoint(lat, lon))
                    }
                    return@withContext roadPoints // Return the beautiful road geometry!
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: If the user has no internet or OSRM is down, just draw straight lines between stops
        return@withContext stops
    }
}