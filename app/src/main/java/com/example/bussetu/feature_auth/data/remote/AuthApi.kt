package com.example.bussetu.feature_auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// 1. What we send to the server
data class LoginRequest(
    val userName: String, // Or username, depending on your backend
    val password: String
)

// 2. What the server sends back
data class LoginResponse(
    val id: Int,       // <-- We need this to save in DataStore!
    val name: String,
    val role: String,
    val token: String? = null // Optional, if your backend uses JWT tokens
)

// 3. The API Endpoint
interface AuthApi {
    @POST("api/auth/login") // Replace with your actual backend endpoint
    suspend fun loginDriver(
        @Body request: LoginRequest
    ): LoginResponse
}