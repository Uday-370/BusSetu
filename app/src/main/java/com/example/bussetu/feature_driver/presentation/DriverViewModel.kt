package com.example.bussetu.feature_driver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bussetu.core.utils.SessionManager
import com.example.bussetu.feature_driver.domain.model.Bus
import com.example.bussetu.feature_driver.domain.model.Route
import com.example.bussetu.feature_driver.domain.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bussetu.feature_driver.service.LocationService
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val repository: DriverRepository,
    private val sessionManager: SessionManager, // Need this to know WHICH driver is starting the trip
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(DriverDashboardState())
    val state: StateFlow<DriverDashboardState> = _state.asStateFlow()

    init {
        // Fetch dropdown data the second the Dashboard opens
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Fetch both at the same time
            val busesResult = repository.getAvailableBuses()
            val routesResult = repository.getRoutes()

            if (busesResult.isSuccess && routesResult.isSuccess) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        availableBuses = busesResult.getOrNull() ?: emptyList(),
                        availableRoutes = routesResult.getOrNull() ?: emptyList()
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dashboard data. Please try again."
                    )
                }
            }
        }
    }

    fun onBusSelected(bus: Bus) {
        _state.update { it.copy(selectedBus = bus) }
    }

    fun onRouteSelected(route: Route) {
        _state.update { it.copy(selectedRoute = route) }
    }

    fun startDuty() {
        val currentBus = _state.value.selectedBus
        val currentRoute = _state.value.selectedRoute

        if (currentBus == null || currentRoute == null) {
            _state.update { it.copy(error = "Please select both a Bus and a Route.") }
            return
        }

        viewModelScope.launch {
            // 1. Tell UI to show the Orange Connecting spinner
            _state.update { it.copy(isLoading = true, error = null) }

            // 2. Fetch the driver ID from DataStore safely
            val driverId = sessionManager.getDriverId.firstOrNull()

            // ✅ THE FIX: If ID is missing, stop spinning and show an error!
            if (driverId == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Driver ID not found. Please log out and log in again."
                    )
                }
                return@launch
            }

            // 3. Make the API call to start the trip
            val result = repository.startTrip(
                busId = currentBus.id,
                routeId = currentRoute.id,
                driverId = driverId
            )

            // 4. Handle Success or Failure
            result.onSuccess { tripId ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isTracking = true, // Triggers the Green ACTIVE state!
                        currentTripId = tripId
                    )
                }

                // ✅ UPDATED: Pass the tripId into the service!
                val serviceIntent = Intent(context, LocationService::class.java).apply {
                    putExtra("EXTRA_TRIP_ID", tripId)
                }
                ContextCompat.startForegroundService(context, serviceIntent)

            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to connect to server."
                    )
                }
            }
        }
    }

    fun stopDuty(onSuccess: () -> Unit) {
        val tripId = _state.value.currentTripId

        // 1. If there's no trip ID, just reset the local state and let them leave immediately!
        if (tripId == null) {
            _state.update {
                it.copy(
                    isTracking = false,
                    isLoading = false,
                    error = null
                )
            }
            onSuccess()
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.endTrip(tripId)

            // 3. Handle the response
            result.onSuccess {
                _state.update {
                    it.copy(
                        isTracking = false,
                        currentTripId = null,
                        isLoading = false
                    )
                }

                // ✅ NEW: Kill the LocationService and remove the notification!
                val serviceIntent = Intent(context, LocationService::class.java)
                context.stopService(serviceIntent)

                // 4. Server confirmed! Now we safely navigate away.
                onSuccess()

            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isTracking = false,
                        currentTripId = null,
                        isLoading = false,
                        error = "Trip ended locally, but server sync failed: ${exception.message}"
                    )
                }

                // ✅ NEW: Kill the service even if the server failed, so we don't track them forever!
                val serviceIntent = Intent(context, LocationService::class.java)
                context.stopService(serviceIntent)

                onSuccess()
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}