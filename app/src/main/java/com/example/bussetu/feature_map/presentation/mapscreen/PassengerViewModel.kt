package com.example.bussetu.feature_map.presentation.mapscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bussetu.feature_map.domain.model.StopStatus // ✅ Ensure this is imported!
import com.example.bussetu.feature_map.domain.repository.PassengerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PassengerViewModel @Inject constructor(
    private val repository: PassengerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    init {
        // ✅ 1. Grab the exact tripId (Int) from Navigation
        val tripId = savedStateHandle.get<Int>("tripId") ?: -1

        // ✅ 2. Temporarily set the Header Text
        _uiState.update {
            it.copy(
                routeCode = "🚍",
                destinationTitle = "Active Trip #$tripId"
            )
        }

        // ✅ 3. Start fetching if we have a valid ID
        if (tripId != -1) {
            loadRouteAndStartTracking(tripId = tripId)
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Invalid Trip ID") }
        }
    }

    // ✅ 4. Load the static map data (Lines and Stops)
    private fun loadRouteAndStartTracking(tripId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.getRouteDetails(tripId)

            result.onSuccess { (routePoints, stops) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        routePoints = routePoints,
                        stops = stops,
                        currentBusLocation = routePoints.firstOrNull()
                    )
                }

                // We don't automatically start live tracking here anymore.
                // It will be started by the UI's Lifecycle (ON_START) to save battery.

            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.localizedMessage) }
            }
        }
    }

    // Helper to calculate Haversine distance
    private fun getDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    // ✅ 5. The Live Tracking Loop with Flow
    fun startLiveTracking() {
        val tripId = savedStateHandle.get<Int>("tripId") ?: -1
        if (tripId == -1 || trackingJob?.isActive == true) return
        
        trackingJob?.cancel()

        trackingJob = viewModelScope.launch {
            repository.getLiveBusLocationFlow(tripId).collect { update ->
                _uiState.update { currentState ->
                    val currentLoc = update.location
                    val etasMap = update.etas

                    // Find closest stop using ETAs if available
                    var activeStopIndex = 0 // Default to 0: no stops passed yet until real GPS data arrives
                    if (etasMap != null && etasMap.isNotEmpty()) {
                        val firstUpcomingIdx = currentState.stops.indexOfFirst { stop ->
                            etasMap[stop.id] != null
                        }
                        if (firstUpcomingIdx != -1) {
                            activeStopIndex = firstUpcomingIdx
                        }
                        // If firstUpcomingIdx == -1, all ETAs are null (bus at start or no GPS fix)
                        // activeStopIndex stays 0 — all stops remain UPCOMING
                    } else {
                        // No ETAs available — use Haversine distance to find closest stop
                        var minDistance = Double.MAX_VALUE
                        currentState.stops.forEachIndexed { index, stop ->
                            val dist = getDistanceInKm(currentLoc.latitude, currentLoc.longitude, stop.location.latitude, stop.location.longitude)
                            if (dist < minDistance) {
                                minDistance = dist
                                activeStopIndex = index
                            }
                        }
                    }

                    // Map the stops with exact ETAs
                    val updatedStops = currentState.stops.mapIndexed { index, stop ->
                        val etaMins = etasMap?.get(stop.id)

                        when {
                            index < activeStopIndex -> {
                                stop.copy(status = StopStatus.COMPLETED, delayMinutes = 0)
                            }
                            index == activeStopIndex -> {
                                stop.copy(status = StopStatus.CURRENT, delayMinutes = etaMins ?: 0)
                            }
                            else -> {
                                stop.copy(status = StopStatus.UPCOMING, delayMinutes = etaMins ?: 0)
                            }
                        }
                    }

                    val activeDelay = updatedStops.getOrNull(activeStopIndex)?.delayMinutes ?: 0

                    currentState.copy(
                        currentBusLocation = currentLoc,
                        currentDelayMinutes = activeDelay,
                        isDelayed = true,
                        stops = updatedStops,
                        error = null
                    )
                }
            }
        }
    }

    fun stopLiveTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveTracking()
    }
}