package com.app.mileagetracker.tracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.app.mileagetracker.R
import com.app.mileagetracker.room_database.AppDatabase
import com.app.mileagetracker.room_database.Journey
import com.app.mileagetracker.ui.view.MainActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.launch

class LocationService : LifecycleService() {
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val pathPoints = mutableListOf<LatLng>()
    private var totalDistance = 0f
    private var startTime = 0L
    private var notificationManager: NotificationManager? = null
    private var isNotificationRunning = false

    private fun formatElapsedTime(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / 1000 / 60 % 60
        val hours = millis / 1000 / 60 / 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()


        if (!hasRequiredPermissions()) {
            stopSelf()
            return
        }

        startForegroundNotification()
        startTime = System.currentTimeMillis()
        startLocationUpdates()
    }


    private fun hasRequiredPermissions(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val fgService = ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
        val fgServiceLocation = if (Build.VERSION.SDK_INT >= 34) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

        return (fineLocation || coarseLocation) && fgService && fgServiceLocation
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startForegroundNotification() {
        createNotificationChannel()
        notificationManager = getSystemService(NotificationManager::class.java)
        isNotificationRunning = true

        startTime = System.currentTimeMillis()

        // Initial Notification
        val initialNotification = NotificationCompat.Builder(this, "track_channel")
            .setContentTitle("Tracking journey")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText("Elapsed Time: 00:00:00")
            .setOngoing(true)
            .build()

        startForeground(1, initialNotification, FOREGROUND_SERVICE_TYPE_LOCATION)

        lifecycleScope.launch {
            while (isNotificationRunning) {
                val elapsed = System.currentTimeMillis() - startTime
                val formattedTime = formatElapsedTime(elapsed)

                val updatedNotification = NotificationCompat.Builder(this@LocationService, "track_channel")
                    .setContentTitle("Tracking journey")
                    .setSmallIcon(R.drawable.img_tracking)
                    .setContentText("Elapsed Time: $formattedTime")
                    .setOngoing(true)
                    .build()

                notificationManager?.notify(1, updatedNotification)

                kotlinx.coroutines.delay(1000)
            }
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "track_channel",
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(10f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (loc in result.locations) {
                    if (loc.accuracy < 20) {
                        val newPoint = LatLng(loc.latitude, loc.longitude)

                        if (pathPoints.isNotEmpty()) {
                            totalDistance += SphericalUtil.computeDistanceBetween(pathPoints.last(), newPoint).toFloat()
                        }
                        pathPoints.add(newPoint)
                    }
                }
            }
        }

        if (!hasRequiredPermissions()) {
            Log.e("LocationService", "Permissions lost during runtime. Stopping service.")
            stopSelf()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        val endTime = System.currentTimeMillis()
        val journey = Journey(
            startTime = startTime,
            endTime = endTime,
            distanceInMeters = totalDistance,
            durationInMiliSeconds = endTime - startTime,
            pathJson = MainActivity.lastPathJson
        )
//

        lifecycleScope.launch {
            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "journey-db").build()
            db.journeyDao().insertJourney(journey)
        }
        isNotificationRunning = false
    }
}