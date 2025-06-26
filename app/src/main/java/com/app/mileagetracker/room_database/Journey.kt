package com.app.mileagetracker.room_database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "journeys")
data class Journey(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val distanceInMeters: Float,
    val pathJson: List<LatLng>
)