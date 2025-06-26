package com.app.mileagetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.app.mileagetracker.room_database.JourneyDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val journeyDao: JourneyDao) : ViewModel() {
    val journeys = journeyDao.getAllJourneys().asLiveData()
}

