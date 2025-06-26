package com.app.mileagetracker.room_database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Journey::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao
}
