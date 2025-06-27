package com.app.mileagetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.mileagetracker.room_database.Journey
import com.app.mileagetracker.room_database.JourneyDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val journeyDao: JourneyDao) : ViewModel() {
    val journeys = journeyDao.getAllJourneys().asLiveData()

//    val latestJourney=journeyDao.getLatestJourney()
//


    private val _latestJourney = MutableStateFlow<Journey?>(null)
    val latestJourney: StateFlow<Journey?> = _latestJourney

    fun fetchLatestJourney() {
        viewModelScope.launch {
            val journey = journeyDao.getLatestJourney()
            _latestJourney.value = journey
        }
    }
}

