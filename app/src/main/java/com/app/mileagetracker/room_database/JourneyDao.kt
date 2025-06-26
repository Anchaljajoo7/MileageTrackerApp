package com.app.mileagetracker.room_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Insert
    suspend fun insertJourney(journey: Journey)

    @Query("SELECT * FROM journeys ORDER BY startTime DESC")
    fun getAllJourneys(): Flow<List<Journey>>
}