package com.example.bussetu


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BusSetuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // We can initialize things like Firebase or OsmDroid configuration here later!
    }
}