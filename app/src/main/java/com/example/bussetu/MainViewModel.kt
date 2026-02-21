package com.example.bussetu

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bussetu.core.navigation.Screen
import com.example.bussetu.core.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    // Keeps the UI hidden until we finish reading the DataStore
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    // Decides which screen to show first
    private val _startDestination = mutableStateOf(Screen.Welcome.route)
    val startDestination: State<String> = _startDestination

    init {
        viewModelScope.launch {
            sessionManager.getDriverId.collect { id ->
                if (id != null) {
                    // ID exists! Skip login and go to the dashboard.
                    _startDestination.value = Screen.DriverDashboard.route
                } else {
                    // No ID. Send them to the Welcome screen.
                    _startDestination.value = Screen.Welcome.route
                }
                _isLoading.value = false // Done loading, safe to draw UI
            }
        }
    }
}