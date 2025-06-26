package com.app.mileagetracker.room_database.converter


import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromLatLngList(value: List<LatLng>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLatLngList(value: String): List<LatLng> {
        val type = object : TypeToken<List<LatLng>>() {}.type
        return Gson().fromJson(value, type)
    }
}
