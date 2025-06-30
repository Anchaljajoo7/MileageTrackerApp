package com.app.mileagetracker.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.mileagetracker.databinding.RowJourneysBinding
import com.app.mileagetracker.room_database.Journey
import com.app.mileagetracker.utils.RecyclerItemClickListner


class JourneysAdapter(
    var context: Context,
    var recyclerItemClickListner: RecyclerItemClickListner, var list: List<Journey>
) : RecyclerView.Adapter<JourneysAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: RowJourneysBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowJourneysBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {

        return list.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvJourneyId.text = "Journey id: ${list[position].id}"
        holder.binding.tvJourneyDistance.text = "Journey distance: ${(list[position].distanceInMeters / 1000)} KM"
        holder.binding.tvJourneyDuration.text = "Journey duration: ${list[position].durationInMiliSeconds / 1000} sec"
        holder.binding.tvJourneyStartTime.text = "Journey starttime: ${formatTime(list[position].startTime)}"
        holder.binding.tvJourneyEndTime.text = "Journey endtime: ${formatTime(list[position].endTime)}"
        Log.d("Anchallllllllllllllllllll", "onBindViewHolder: " + list.size)

    }

    fun formatTime(millis: Long): String {
        val date = java.util.Date(millis)
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return format.format(date)
    }


}