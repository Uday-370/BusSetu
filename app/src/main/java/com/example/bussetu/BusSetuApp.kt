package com.example.bussetu

import android.app.Application
import com.example.bussetu.core.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BusSetuApp : Application() {

    // Hilt injects SessionManager here after the component graph is built
    @Inject
    lateinit var sessionManager: SessionManager

    // App-level scope — lives as long as the process (no memory leak risk)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Restore the JWT token from DataStore into the in-memory cache.
        // This runs once on startup and ensures the network interceptor has the
        // token ready immediately — even after the user force-closed and reopened the app.
        appScope.launch {
            sessionManager.restoreCachedToken()
        }
    }
}