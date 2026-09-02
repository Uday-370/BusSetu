package com.example.bussetu.feature_auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// ─── What we send to the server ───────────────────────────────────────────────
// Backend expects: { "email": "...", "password": "..." }
data class LoginRequest(
    val email: String,
    val password: String
)

// ─── Nested user object inside the response ────────────────────────────────────
// Backend returns: { "id": 1, "name": "...", "email": "...", "role": "driver" }
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

// ─── Full response from POST /api/auth/login ───────────────────────────────────
// Backend returns: { "message": "Login successful", "token": "...", "user": { ... } }
data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserDto
)

// ─── The API endpoint ─────────────────────────────────────────────────────────
interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}