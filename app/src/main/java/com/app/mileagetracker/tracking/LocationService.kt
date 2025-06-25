package com.app.mileagetracker.tracking

import android.location.LocationRequest
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.app.mileagetracker.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : LifecycleService() {
//    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
//    private val pathPoints = mutableListOf<LatLng>()
//    private var totalDistance = 0f
//    private var startTime = 0L
//
//    override fun onCreate() {
//        super.onCreate()
//        startTime = System.currentTimeMillis()
//        startForegroundNotification()
//        startLocationUpdates()
//    }
//
//    private fun startForegroundNotification() {
//        val notification = NotificationCompat.Builder(this, "track_channel")
//            .setContentTitle("Tracking journey")
//            .setSmallIcon(R.drawable.ic_location)
//            .setContentText("Tracking ongoing")
//            .build()
//        startForeground(1, notification)
//    }
//
//    private fun startLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
//            .setMinUpdateDistanceMeters(10f)
//            .build()
//
//        val callback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                super.onLocationResult(result)
//                for (loc in result.locations) {
//                    if (loc.accuracy < 20) {
//                        val newPoint = LatLng(loc.latitude, loc.longitude)
//                        if (pathPoints.isNotEmpty()) {
//                            totalDistance += SphericalUtil.computeDistanceBetween(pathPoints.last(), newPoint).toFloat()
//                        }
//                        pathPoints.add(newPoint)
//                    }
//                }
//            }
//        }
//        fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        val endTime = System.currentTimeMillis()
//        val journey = Journey(
//            startTime = startTime,
//            endTime = endTime,
//            distanceInMeters = totalDistance,
//            pathJson = Gson().toJson(pathPoints)
//        )
//        lifecycleScope.launch {
//            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "journey-db").build()
//            db.journeyDao().insertJourney(journey)
//        }
//    }
}