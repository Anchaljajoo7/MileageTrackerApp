package com.app.mileagetracker.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.mileagetracker.R
import com.app.mileagetracker.databinding.ActivityMapPreviewBinding
import com.app.mileagetracker.ui.view.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapPreviewActivity : AppCompatActivity() {
    private lateinit var activityMapPreviewBinding: ActivityMapPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMapPreviewBinding=ActivityMapPreviewBinding.inflate(layoutInflater)
        setContentView(activityMapPreviewBinding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            val pathPoints= MainActivity.lastPathJson
            Log.d("Anchal", "onCreate:map activity "+pathPoints)
            val polyline = PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(8f)
            googleMap.addPolyline(polyline)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.first(), 15f))
        }
    }
}