package com.example.bussetu.feature_auth.data.repository

import com.example.bussetu.core.utils.SessionManager
import com.example.bussetu.feature_auth.data.remote.AuthApi
import com.example.bussetu.feature_auth.data.remote.LoginRequest
import com.example.bussetu.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(userName: String, password: String): Result<Unit> {
        return try {
            // 1. Send credentials to your PostgreSQL backend via Retrofit
            val response = api.loginDriver(LoginRequest(userName = userName, password = password))

            // 2. Validate role based on your database schema
            if (response.role != "driver") {
                return Result.failure(Exception("Access denied. Only drivers can log in here."))
            }

            // 3. Save the 'id' to DataStore so they stay logged in
            sessionManager.saveDriverId(response.id)

            // 4. Return success to the ViewModel
            Result.success(Unit)
        } catch (e: Exception) {
            // Catches network errors, 401 Unauthorized, etc.
            Result.failure(e)
        }
    }
}