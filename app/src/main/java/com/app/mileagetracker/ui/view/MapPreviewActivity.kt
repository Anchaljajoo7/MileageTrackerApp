package com.app.mileagetracker.ui.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.mileagetracker.R
import com.app.mileagetracker.databinding.ActivityMapPreviewBinding
import com.app.mileagetracker.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
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
        val type = intent.getStringExtra("AllJourneyScreen")
        Log.d("Anchal", "onCreate: "+type)
        if (type.equals("AllJourneyScreen")) {
            Log.d("Anchal", "onCreate: all journey")
            intentData()
        } else {
            Log.d("Anchal", "onCreate: journey")
            initialSetup()
        }

        clickEvent()

    }

    private fun intentData() {


        val startTime = intent.getLongExtra("starttime", 0L)
        val endTime = intent.getLongExtra("endtime", 0L)
        val duration = intent.getLongExtra("duration", 0L)
        val distance = intent.getFloatExtra("distance", 0f)

        activityMapPreviewBinding.tvTotalDuration.text =
            "Total Duration: ${duration / 1000} sec"

        activityMapPreviewBinding.tvTotalDistance.text =
            "Total Distance: ${String.format("%.2f", distance / 1000)} KM"

        activityMapPreviewBinding.tvStartEndTime.text =
            "Start: ${formatTime(startTime)} | End: ${formatTime(endTime)}"

    }

    private fun clickEvent() {
        activityMapPreviewBinding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun initialSetup() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            val pathPoints= MainActivity.lastPathJson
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            googleMap.uiSettings.isMapToolbarEnabled = true
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