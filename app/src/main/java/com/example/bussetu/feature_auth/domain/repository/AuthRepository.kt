package com.example.bussetu.feature_auth.domain.repository

// We use Kotlin's built-in Result class to cleanly handle success or failure.
interface AuthRepository {
    suspend fun login(userName: String, password: String): Result<Unit>
}