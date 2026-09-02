package com.example.bussetu.feature_auth.data.repository

import com.example.bussetu.core.utils.SessionManager
import com.example.bussetu.feature_auth.data.remote.AuthApi
import com.example.bussetu.feature_auth.data.remote.LoginRequest
import com.example.bussetu.feature_auth.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(userName: String, password: String): Result<Unit> {
        return try {
            // userName parameter holds the email entered by the driver (Option A)
            val response = api.login(LoginRequest(email = userName, password = password))

            // ─── Role Guard ───────────────────────────────────────────────────────
            // This app is ONLY for drivers. Admins/passengers must not be able to log in.
            if (response.user.role != "driver") {
                return Result.failure(
                    Exception("Access denied. This app is for drivers only.")
                )
            }

            // ─── Save Session ─────────────────────────────────────────────────────
            // Saves both the driver ID and JWT token.
            // The in-memory cache updates instantly so the interceptor is ready immediately.
            sessionManager.saveSession(
                driverId   = response.user.id,
                token      = response.token,
                driverName = response.user.name
            )

            Result.success(Unit)

        } catch (e: HttpException) {
            // ─── HTTP Error Responses (4xx / 5xx) ────────────────────────────────
            // Turn raw HTTP codes into messages the driver can actually understand.
            val message = when (e.code()) {
                400 -> "Incorrect email or password. Please try again."
                401 -> "Session expired. Please log in again."
                403 -> "Access denied."
                404 -> "Account not found. Please check your email."
                500 -> "Server error. Please try again in a moment."
                else -> "Login failed (Error ${e.code()}). Please try again."
            }
            Result.failure(Exception(message))

        } catch (e: IOException) {
            // ─── Network Error (no internet, timeout, DNS failure) ────────────────
            Result.failure(Exception("No internet connection. Please check your network and try again."))

        } catch (e: Exception) {
            // ─── Unexpected Error ─────────────────────────────────────────────────
            Result.failure(Exception("Something went wrong. Please try again."))
        }
    }
}