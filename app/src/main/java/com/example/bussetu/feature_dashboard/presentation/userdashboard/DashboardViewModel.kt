package com.example.bussetu.feature_dashboard.presentation.userdashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bussetu.feature_map.domain.repository.PassengerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: PassengerRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("bussetu_search_history", Context.MODE_PRIVATE)
    private val HISTORY_KEY = "recent_searches"
    private val MAX_HISTORY = 5

    // 1. Data for the UI Dropdowns
    private val _stopSuggestions = MutableStateFlow<List<String>>(emptyList())
    val stopSuggestions = _stopSuggestions.asStateFlow()

    // 2. Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 3. Navigation Event
    private val _navigateToMap = MutableSharedFlow<Int>()
    val navigateToMap = _navigateToMap.asSharedFlow()

    // 4. Error handling
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    // 5. Recent search history (list of "Start → End" strings)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches = _recentSearches.asStateFlow()

    init {
        loadStopsForDropdown()
        loadRecentSearches()
    }

    private fun loadStopsForDropdown() {
        viewModelScope.launch {
            val result = repository.getAllStopNames()
            result.onSuccess { uniqueStops ->
                _stopSuggestions.value = uniqueStops
            }.onFailure {
                _stopSuggestions.value = emptyList()
            }
        }
    }

    private fun loadRecentSearches() {
        val saved = prefs.getStringSet(HISTORY_KEY, emptySet()) ?: emptySet()
        // Preserve insertion order via a separate ordered list stored as joined string
        val ordered = prefs.getString("${HISTORY_KEY}_ordered", "") ?: ""
        _recentSearches.value = if (ordered.isNotEmpty()) {
            ordered.split("|||").filter { it.isNotEmpty() }.take(MAX_HISTORY)
        } else {
            saved.take(MAX_HISTORY).toList()
        }
    }

    private fun saveSearchToHistory(entry: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(entry) // remove duplicate if exists
        current.add(0, entry) // add to front
        val trimmed = current.take(MAX_HISTORY)
        _recentSearches.value = trimmed
        prefs.edit()
            .putStringSet(HISTORY_KEY, trimmed.toSet())
            .putString("${HISTORY_KEY}_ordered", trimmed.joinToString("|||"))
            .apply()
    }

    fun searchByRoute(startStop: String, endStop: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.searchActiveTripByStops(startStop, endStop)
            result.onSuccess { tripId ->
                saveSearchToHistory("$startStop → $endStop")
                _navigateToMap.emit(tripId)
            }.onFailure { error ->
                val msg = if (error is java.io.IOException)
                    "Network error. Please check your connection."
                else
                    error.message ?: "No active bus found for this route."
                _errorMessage.emit(msg)
            }
            _isLoading.value = false
        }
    }

    fun searchByBusNumber(busNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.searchActiveTripByBusNumber(busNumber)
            result.onSuccess { tripId ->
                saveSearchToHistory("Bus $busNumber")
                _navigateToMap.emit(tripId)
            }.onFailure { error ->
                val msg = if (error is java.io.IOException)
                    "Network error. Please check your connection."
                else
                    error.message ?: "Bus $busNumber is not currently active."
                _errorMessage.emit(msg)
            }
            _isLoading.value = false
        }
    }

    fun onRecentSearchClick(entry: String, onStartChange: (String) -> Unit, onEndChange: (String) -> Unit) {
        // Parse "Start → End" format back into fields
        if (entry.contains(" → ")) {
            val parts = entry.split(" → ")
            onStartChange(parts[0])
            if (parts.size > 1) onEndChange(parts[1])
        }
        // "Bus XXX" format — nothing to fill in route fields
    }
}