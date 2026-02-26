package com.example.bussetu.feature_driver.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.bussetu.MainActivity
import com.example.bussetu.feature_driver.domain.repository.DriverRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ REQUIRED FOR HILT TO WORK IN A SERVICE
@AndroidEntryPoint
class LocationService : Service() {

    // Inject the repository to talk to the server
    @Inject
    lateinit var repository: DriverRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Coroutine scope for network calls
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Store the ID of the trip we are currently tracking
    private var currentTripId: Int = -1

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                for (location in locationResult.locations) {
                    val lat = location.latitude
                    val lng = location.longitude

                    Log.d("BusSetu_GPS", "Live Bus Location: Lat: $lat, Lng: $lng")

                    // ✅ BROADCAST TO SERVER!
                    if (currentTripId != -1) {
                        serviceScope.launch {
                            val result = repository.updateLocation(currentTripId, lat, lng)
                            if (result.isFailure) {
                                Log.e("BusSetu_GPS", "Failed to send location to server")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ✅ Grab the Trip ID sent from the ViewModel
        currentTripId = intent?.getIntExtra("EXTRA_TRIP_ID", -1) ?: -1

        createNotificationChannel()

        val pendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification = NotificationCompat.Builder(this, "LOCATION_CHANNEL_ID")
            .setContentTitle("📍 BusSetu Live Tracking")
            .setContentText("Broadcasting your location to passengers.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setColor(Color.parseColor("#1D4ED8"))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(1, notification)
        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // ✅ Clean up memory when the service dies
        serviceScope.cancel()
        Log.d("BusSetu_GPS", "Tracking Stopped. Battery saved.")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "LOCATION_CHANNEL_ID",
                "Live Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}