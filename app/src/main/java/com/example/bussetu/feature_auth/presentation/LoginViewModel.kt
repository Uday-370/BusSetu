package com.example.bussetu.feature_auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bussetu.feature_auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Internal mutable state
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    // Public immutable state that the UI observes
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        // Basic validation before hitting the network
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please enter both username and password")
            return
        }

        // Tell the UI to show the loading spinner
        _loginState.value = LoginState.Loading

        // Launch a coroutine to do the background network work
        viewModelScope.launch {
            val result = repository.login(email, password)

            // The built-in Kotlin Result class makes this extremely clean
            result.onSuccess {
                _loginState.value = LoginState.Success
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "An unknown error occurred")
            }
        }
    }

    // Call this after showing a Snackbar so it doesn't show again on rotation
    fun resetState() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
}