package com.app.mileagetracker.ui.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.mileagetracker.databinding.ActivityMainBinding
import com.app.mileagetracker.tracking.LocationService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var activityMainBinding: ActivityMainBinding
    private var isTracking = false
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var totalDistance = 0f
    private val pathPoints = mutableListOf<LatLng>()
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        initialSetup()
        clickEvent()

    }

    companion object {
        var lastPathJson: MutableList<LatLng> = mutableListOf()
    }

    private fun isServiceRunning(): Boolean {
        val activityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == LocationService::class.java.name) {
                return true
            }
        }
        return false
    }

    private fun initialSetup() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        updateButtonVisibility(isServiceRunning())

        val running = isServiceRunning()
        Toast.makeText(
            this,
            "Service is ${if (running) "already" else "not"} running",
            Toast.LENGTH_SHORT
        ).show()



        if(running){

            activityMainBinding.tvSteps.setText("Total steps: $totalSteps")
        }
        else{
            activityMainBinding.tvSteps.setText("Total steps: 0")
        }


        if (!hasAllRequiredPermissions()) {
            requestInitialPermissions()
        }
    }

    private fun updateButtonVisibility(isRunning: Boolean) {
        activityMainBinding.startBtn.visibility =
            if (isRunning) android.view.View.GONE else android.view.View.VISIBLE
        activityMainBinding.endBtn.visibility =
            if (isRunning) android.view.View.VISIBLE else android.view.View.GONE
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun clickEvent() {

        activityMainBinding.tvAllJournies.setOnClickListener {
            startActivity(Intent(this@MainActivity,JourneysActivity::class.java))
        }


        activityMainBinding.startBtn.setOnClickListener {

            if (hasAllRequiredPermissions()) {

                startLocationService()
                val running = isServiceRunning()
                Toast.makeText(
                    this,
                    "Service is ${if (running) "started" else "not"} ",
                    Toast.LENGTH_SHORT
                ).show()
                totalSteps=0F
                previousTotalSteps = 0F
                activityMainBinding.tvSteps.text = "Total steps: 0"

                updateButtonVisibility(true)
            } else {
                requestLocationPermissions()
            }
        }

        activityMainBinding.endBtn.setOnClickListener {
            val intent = Intent(this, LocationService::class.java)
            stopService(intent)
            isTracking = false
            totalSteps = 0f
            previousTotalSteps = 0f

            val running = isServiceRunning()
            Toast.makeText(
                this,
                "Service is ${if (running) "not" else "ended"} ",
                Toast.LENGTH_SHORT
            ).show()



            val mapIntent = Intent(this@MainActivity, MapPreviewActivity::class.java)
            startActivity(mapIntent)
        }

    }


    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                showSettingsDialog()
                Toast.makeText(this, "Some permissions were permanently denied. Please enable them from settings.", Toast.LENGTH_SHORT).show()
            } else {
//                startLocationService()
            }
        }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE)
        permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }


        val permanentlyDenied = permissionsToRequest.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED &&
                    !shouldShowRequestPermissionRationale(permission)
        }


        if (permanentlyDenied) {
            showSettingsDialog()
        } else {
            permissionRequestLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Some permissions were permanently denied. Please enable them from settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }





    private fun hasAllRequiredPermissions(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fgService = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
        val activityRecognition = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)

        val fgLocation = if (Build.VERSION.SDK_INT >= 34) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        } else PackageManager.PERMISSION_GRANTED

        val notificationPermission = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        } else PackageManager.PERMISSION_GRANTED

        return (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) &&
                fgService == PackageManager.PERMISSION_GRANTED &&
                fgLocation == PackageManager.PERMISSION_GRANTED
                &&  activityRecognition == PackageManager.PERMISSION_GRANTED &&
                notificationPermission == PackageManager.PERMISSION_GRANTED
    }


    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        isTracking = true
    }



    private fun requestInitialPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.ACTIVITY_RECOGNITION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionRequestLauncher.launch(permissions.toTypedArray())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (isServiceRunning() == false) event.values[0] = 0F
            totalSteps = event.values[0]
            val currentSteps = totalSteps - previousTotalSteps
            activityMainBinding.tvSteps.text = "Total steps: $currentSteps"
            startLocationUpdates()
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
                        lastPathJson.addAll(pathPoints)
                    }
                }
            }
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)

    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility(isServiceRunning())
    }



}