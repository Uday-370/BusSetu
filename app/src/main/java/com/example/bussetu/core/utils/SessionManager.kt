package com.example.bussetu.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bussetu_auth_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Keys for DataStore (persistent storage across app restarts)
        val DRIVER_ID_KEY  = intPreferencesKey("driver_id")
        val JWT_TOKEN_KEY  = stringPreferencesKey("jwt_token")
        val DRIVER_NAME_KEY = stringPreferencesKey("driver_name")
    }

    // ─── In-memory token cache ────────────────────────────────────────────────────
    // OkHttp interceptors run on a background thread and CANNOT use suspend functions.
    // We keep the token in memory so the interceptor can read it instantly (no blocking).
    // This is populated at login and restored from DataStore on app startup.
    @Volatile
    var cachedToken: String? = null
        private set

    // ─── Save (called after successful login) ─────────────────────────────────────
    suspend fun saveSession(driverId: Int, token: String, driverName: String) {
        cachedToken = token   // Update in-memory cache immediately
        context.dataStore.edit { prefs ->
            prefs[DRIVER_ID_KEY]   = driverId
            prefs[JWT_TOKEN_KEY]   = token
            prefs[DRIVER_NAME_KEY] = driverName
        }
    }

    // ─── Read (for ViewModels and UI) ─────────────────────────────────────────────
    val getDriverId: Flow<Int?> = context.dataStore.data.map { it[DRIVER_ID_KEY] }
    val getToken: Flow<String?> = context.dataStore.data.map { it[JWT_TOKEN_KEY] }
    val getDriverName: Flow<String?> = context.dataStore.data.map { it[DRIVER_NAME_KEY] }

    // ─── Restore in-memory cache on app startup ───────────────────────────────────
    // Call this once from the Application class so the interceptor works immediately.
    suspend fun restoreCachedToken() {
        cachedToken = context.dataStore.data.map { it[JWT_TOKEN_KEY] }.firstOrNull()
    }

    // ─── Logout (clear everything) ────────────────────────────────────────────────
    suspend fun clearSession() {
        cachedToken = null    // Clear in-memory cache immediately
        context.dataStore.edit { it.clear() }
    }

    // ─── Check if a session exists ────────────────────────────────────────────────
    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.map { it[JWT_TOKEN_KEY] }.firstOrNull() != null
    }
}