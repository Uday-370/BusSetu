package com.example.bussetu.feature_auth.data.repository

import com.example.bussetu.core.utils.SessionManager
import com.example.bussetu.feature_auth.data.remote.AuthApi
import com.example.bussetu.feature_auth.data.remote.LoginRequest
import com.example.bussetu.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(userName: String, password: String): Result<Unit> {
        return try {
            // --- FAKE BACKEND FOR TESTING ---

            // 1. Simulate a 2-second network call
            delay(2000)

            // 2. Check for hardcoded credentials
            if (userName == "driver" && password == "1234") {
                // SUCCESS: Save fake ID #99 so the Dashboard knows who is driving
                val fakeDriverId = 99
                sessionManager.saveDriverId(fakeDriverId)
                return Result.success(Unit)
            } else {
                // FAILURE: Wrong credentials
                return Result.failure(Exception("Incorrect username or password!"))
            }

            // --------------------------------

            /* --- REAL BACKEND CODE (Uncomment when API is ready) ---
            val response = api.loginDriver(LoginRequest(userName = userName, password = password))

            if (response.role != "driver") {
                return Result.failure(Exception("Access denied. Only drivers can log in here."))
            }

            sessionManager.saveDriverId(response.id)
            Result.success(Unit)
            -------------------------------------------------------- */

        } catch (e: Exception) {
            // Catches network errors, 401 Unauthorized, etc.
            Result.failure(e)
        }
    }
}