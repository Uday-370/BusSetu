package com.example.bussetu.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
        // The key used to save the ID from your PostgreSQL 'users' table
        val DRIVER_ID_KEY = intPreferencesKey("driver_id")
    }

    // Save the ID after successful login
    suspend fun saveDriverId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[DRIVER_ID_KEY] = id
        }
    }

    // Read the ID (Returns null if they haven't logged in yet)
    val getDriverId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[DRIVER_ID_KEY]
    }

    // Clear the ID (For Logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}