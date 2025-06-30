package com.app.mileagetracker.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.mileagetracker.R
import com.app.mileagetracker.databinding.ActivityMapPreviewBinding
import com.app.mileagetracker.ui.view.MainActivity
import com.app.mileagetracker.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapPreviewActivity : AppCompatActivity() {
    private lateinit var activityMapPreviewBinding: ActivityMapPreviewBinding
    val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMapPreviewBinding=ActivityMapPreviewBinding.inflate(layoutInflater)
        setContentView(activityMapPreviewBinding.root)
        initialSetup()
        clickEvent()

    }

    private fun clickEvent() {
        activityMapPreviewBinding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun initialSetup() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            Log.d("Anchallllllll", "initialSetup: " + MainActivity.lastPathJson)
            val pathPoints= MainActivity.lastPathJson

            Log.d("Anchal", "onCreate:map activity "+pathPoints)
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            googleMap.uiSettings.isMapToolbarEnabled = true
//            googleMap.uiSettings.isCompassEnabled = true
            val polyline = PolylineOptions().addAll(pathPoints)
                .color(Color.YELLOW).width(10f)
            googleMap.addPolyline(polyline)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.first(), 25f))
            googleMap.addMarker(MarkerOptions().position(pathPoints.first()).title("Start"))
            googleMap.addMarker(MarkerOptions().position(pathPoints.last()).title("End"))
        }




        mainViewModel.fetchLatestJourney()

        lifecycleScope.launchWhenStarted {
            mainViewModel.latestJourney.collect { journey ->
                journey?.let {
                    activityMapPreviewBinding.tvTotalDuration.text =
                        "Total Duration: ${it.durationInMiliSeconds / 1000} sec"

                    activityMapPreviewBinding.tvTotalDistance.text =
                        "Total Distance: ${(it.distanceInMeters / 1000)} KM"

                    Log.d("Anchal", "initialSetup: "+it.distanceInMeters)
                    activityMapPreviewBinding.tvStartEndTime.text =
                        "Start: ${formatTime(it.startTime)} | End: ${formatTime(it.endTime)}"
                }
            }
        }

    }

    fun formatTime(millis: Long): String {
        val date = java.util.Date(millis)
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return format.format(date)
    }

}