package com.app.mileagetracker.di

import android.content.Context
import androidx.room.Room
import com.app.mileagetracker.room_database.AppDatabase
import com.app.mileagetracker.room_database.JourneyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

// com.app.mileagetracker.di.DatabaseModule.kt

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "journey-db"
        ).build()
    }

    @Provides
    fun provideJourneyDao(appDatabase: AppDatabase): JourneyDao {
        return appDatabase.journeyDao()
    }
}
