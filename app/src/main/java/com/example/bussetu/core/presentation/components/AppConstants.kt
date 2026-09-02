package com.example.bussetu.core.presentation.components

object AppConstants {
    const val APP_NAME = "TrackMyBus"

    // ─── Backend URL ────────────────────────────────────────────────────────────
    // HOW TO GET THIS URL:
    //   1. Start your backend:  cd backend && node server.js
    //   2. Run ngrok tunnel:    ngrok http 5000
    //   3. Copy the HTTPS URL shown (e.g. https://abc123.ngrok-free.app)
    //   4. Paste it below — MUST end with a trailing slash /
    //
    // ⚠️  ngrok free tier generates a NEW URL each time you restart it.
    //     Update this constant and rebuild the app whenever that happens.
    // ─────────────────────────────────────────────────────────────────────────────
    const val BASE_URL = "https://morale-rarity-turbine.ngrok-free.dev/"

    // ─── GPS Tracking ────────────────────────────────────────────────────────────
    // How often the driver's phone sends GPS to the server (milliseconds)
    const val LOCATION_UPDATE_INTERVAL = 5000L       // Normal: every 5 seconds
    const val LOCATION_FASTEST_INTERVAL = 2000L      // Fastest allowed: every 2 seconds

    // ─── Passenger Polling ───────────────────────────────────────────────────────
    // How often the passenger map screen polls for live bus location
    const val PASSENGER_POLL_INTERVAL = 5000L        // Every 5 seconds

    // ─── Network ─────────────────────────────────────────────────────────────────
    const val CONNECT_TIMEOUT_SECONDS = 15L          // Give up connecting after 15s
    const val READ_TIMEOUT_SECONDS    = 30L          // Give up reading after 30s
}
