package com.app.mileagetracker.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.mileagetracker.R
import com.app.mileagetracker.databinding.ActivityMapPreviewBinding
import com.app.mileagetracker.ui.view.MainActivity
import com.app.mileagetracker.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
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
            val pathPoints= MainActivity.lastPathJson
            Log.d("Anchal", "onCreate:map activity "+pathPoints)
            val polyline = PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(8f)
            googleMap.addPolyline(polyline)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.first(), 15f))
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
                    Toast.makeText(this@MapPreviewActivity, ""+it.distanceInMeters, Toast.LENGTH_SHORT).show()
//
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