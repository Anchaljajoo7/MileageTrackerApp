package com.app.mileagetracker.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mileagetracker.databinding.ActivityJourneysBinding
import com.app.mileagetracker.room_database.Journey
import com.app.mileagetracker.ui.adapter.JourneysAdapter
import com.app.mileagetracker.ui.viewmodel.MainViewModel
import com.app.mileagetracker.utils.RecyclerItemClickListner
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JourneysActivity : AppCompatActivity(),RecyclerItemClickListner {
    lateinit var activityJourneysBinding: ActivityJourneysBinding
    val mainViewModel: MainViewModel by viewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: JourneysAdapter
  lateinit var  journeyList:List<Journey>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityJourneysBinding = ActivityJourneysBinding.inflate(layoutInflater)
        setContentView(activityJourneysBinding.root)
        initialSetup()
        clickEvent()

    }

    private fun clickEvent() {
        activityJourneysBinding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun initialSetup() {

        mainViewModel.fetchAllJourneys()
        lifecycleScope.launchWhenStarted {
            mainViewModel.allJourneys.collect { journey ->
                journey?.let {
                    journeyList=journey
                    if(journey.size>0){
                        activityJourneysBinding.rvJourneysList.visibility=View.VISIBLE
                        activityJourneysBinding.tvNoJourneys.visibility=View.GONE
                        linearLayoutManager = LinearLayoutManager(this@JourneysActivity, RecyclerView.VERTICAL, false)
                        activityJourneysBinding.rvJourneysList.layoutManager = linearLayoutManager
                        adapter = JourneysAdapter(this@JourneysActivity, this@JourneysActivity,journey)
                        activityJourneysBinding.rvJourneysList.adapter = adapter
                        Log.d("Anchaaaalllllllllllll", "onCreate: "+journey)
                    }
                    else{
                        activityJourneysBinding.tvNoJourneys.visibility=View.VISIBLE
                        activityJourneysBinding.rvJourneysList.visibility=View.GONE
                    }


                }
            }
        }

    }

    override fun onItemClick(type: String, position: Int) {
        if (type.equals("onItemClick")) {
            val intent = Intent(this@JourneysActivity, MapPreviewActivity::class.java)
            intent.putExtra("AllJourneyScreen","AllJourneyScreen")
            intent.putExtra("endtime",journeyList[position].endTime)
            intent.putExtra("starttime",journeyList[position].startTime)
            intent.putExtra("duration",journeyList[position].durationInMiliSeconds)
            intent.putExtra("distance",journeyList[position].distanceInMeters)
            intent.putParcelableArrayListExtra("latlong", ArrayList(journeyList[position].pathJson))
            startActivity(intent)
        }
    }
}