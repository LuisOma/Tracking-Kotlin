package com.example.tracking.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracking.db.Track
import com.example.tracking.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.tracking.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.tracking.utils.Constants.ACTION_STOP_SERVICE
import com.example.tracking.utils.SortType
import com.example.tracking.utils.Sortable
import com.example.tracking.repositories.MainRepository
import com.example.tracking.repositories.TrackingRepository
import com.example.tracking.services.TrackingService
import kotlinx.coroutines.launch
import java.util.*

class TrackingViewModel @ViewModelInject constructor(private val mainRepository: MainRepository) : ViewModel(), Sortable {

    val isTracking = TrackingRepository.isTracking
    val pathPoints = TrackingRepository.pathPoints
    val currentTimeInMillis = TrackingRepository.timeTrackInMillis
    val distanceInMeters = TrackingRepository.distanceInMeters

    override val tracksSortedByDate = mainRepository.getAllTracksSortedByDate()
    override val tracksSortedByTime = mainRepository.getAllTracksSortedByTimeInMillis()
    override val tracksSortedByDistance = mainRepository.getAllTracksSortedByDistance()
    override val tracks = MediatorLiveData<List<Track>>()
    override var sortType = SortType.DATE

    init {
        fillSources()
    }

    fun sendCommandToService(context: Context) {
        val action =
            if (isTracking.value!!) ACTION_PAUSE_SERVICE
            else ACTION_START_OR_RESUME_SERVICE
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }

    fun setCancelCommand(context: Context) {
        Intent(context, TrackingService::class.java).also {
            it.action = ACTION_STOP_SERVICE
            context.startService(it)
        }
    }

    fun processTrack(context: Context, bitmap: Bitmap, routeName: String) {
        val dateTimestamp = Calendar.getInstance().timeInMillis
        val distance = distanceInMeters.value!!
        val track = Track(
            bitmap,
            dateTimestamp,
            distance,
            currentTimeInMillis.value!!,
            routeName
        )

        viewModelScope.launch {
            setCancelCommand(context)
            mainRepository.insertTrack(track)
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            mainRepository.deleteTrack(track)
        }
    }

    fun restoreDeletedTrack(track: Track) {
        viewModelScope.launch {
            mainRepository.insertTrack(track)
        }
    }
}
