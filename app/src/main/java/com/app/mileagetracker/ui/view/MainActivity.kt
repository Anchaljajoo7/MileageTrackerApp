package com.app.mileagetracker.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.mileagetracker.databinding.ActivityMainBinding
import com.app.mileagetracker.tracking.LocationService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var activityMainBinding: ActivityMainBinding
    private var isTracking = false
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

    private fun initialSetup() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        if (!hasAllRequiredPermissions()) {
            requestInitialPermissions()
        }
    }

    private fun clickEvent() {

        activityMainBinding.startBtn.setOnClickListener {
            Log.d("Anchal", "Checking permissions before starting service")
            if (hasAllRequiredPermissions()) {

                Log.d("Anchal", "onCreate: alllllllllll")
                startLocationService()
            } else {
                requestLocationPermissions()
            }
        }

        activityMainBinding.endBtn.setOnClickListener {
            val intent = Intent(this, LocationService::class.java)
            stopService(intent)
            isTracking = false
        }

    }


    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                showSettingsDialog()
                Toast.makeText(this, "All location permissions are required!", Toast.LENGTH_SHORT).show()
            } else {
//                startLocationService()
            }
        }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Add all required permissions
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE)
        permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
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

        return (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) &&
                fgService == PackageManager.PERMISSION_GRANTED &&
                fgLocation == PackageManager.PERMISSION_GRANTED
                &&  activityRecognition == PackageManager.PERMISSION_GRANTED
    }


    private fun startLocationService() {
        Log.d("Anchal", "startLocationService: ")
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
        permissionRequestLauncher.launch(permissions.toTypedArray())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = event.values[0]
            val currentSteps = totalSteps - previousTotalSteps
            Log.d("Anchal", "onSensorChanged: "+currentSteps)
            Toast.makeText(this@MainActivity, "total steps:"+totalSteps, Toast.LENGTH_SHORT).show()
            Toast.makeText(this@MainActivity, "current steps:"+currentSteps, Toast.LENGTH_SHORT).show()
//            activityMainBinding.stepsTextView.text = "Steps: $currentSteps"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }



}