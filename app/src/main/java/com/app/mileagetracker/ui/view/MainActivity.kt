package com.app.mileagetracker.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.mileagetracker.databinding.ActivityMainBinding
import com.app.mileagetracker.tracking.LocationService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private var isTracking = false

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                showSettingsDialog()
                Toast.makeText(this, "All location permissions are required!", Toast.LENGTH_SHORT).show()
            } else {
                startLocationService()
            }
        }


    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Add all required permissions
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
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
            .setMessage("Location permissions are required to track your journey. Please enable them from app settings.")
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
        val fgLocation = if (Build.VERSION.SDK_INT >= 34) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        } else PackageManager.PERMISSION_GRANTED

        return (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) &&
                fgService == PackageManager.PERMISSION_GRANTED &&
                fgLocation == PackageManager.PERMISSION_GRANTED
    }


    private fun startLocationService() {
        Log.d("Anchal", "startLocationService: ")
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        isTracking = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        if (!hasAllRequiredPermissions()) {
            requestInitialPermissions()
        }

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

        // Optional: Prompt to disable battery optimization
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//            startActivity(intent)
//        }
    }

    private fun requestInitialPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
        permissionRequestLauncher.launch(permissions.toTypedArray())
    }

//
//    private fun requestBackgroundLocationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
//            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionRequestLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
//        }
//    }
//
//    val permissionRequestLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val allGranted = permissions.all { it.value }
//
//        if (allGranted) {
//            requestBackgroundLocationPermission() // ask second-stage only if needed
//        } else {
//            Toast.makeText(this, "Location permissions are required!", Toast.LENGTH_SHORT).show()
//        }
//    }



}