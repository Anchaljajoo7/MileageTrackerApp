package com.app.mileagetracker.room_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mileagetracker.room_database.converter.Converters

@Database(entities = [Journey::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao
}
