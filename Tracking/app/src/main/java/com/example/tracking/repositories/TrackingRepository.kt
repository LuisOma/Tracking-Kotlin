package com.example.tracking.repositories

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.example.tracking.db.UserInfo
import com.example.tracking.utils.Constants
import com.example.tracking.utils.TargetType
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingRepository @Inject constructor(
    private val userInfo: UserInfo
) {

    var isFirstTrack = true
        private set
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeTrack = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    var isCancelled = false
    private var lastCounted = 0

    fun initStartingValues() {
        pathPoints.value = mutableListOf()
        timeTrackInMillis.postValue(0L)
        timeTrackInSeconds.postValue(0L)
        lapTime = 0L
        timeTrack = 0L
        timeStarted = 0L
        lastSecondTimestamp = 0L
    }

    private fun startTracking() {
        addEmptyPolyline()
        isTracking.value = true
        isCancelled = false

        targetType.value = userInfo.targetType
    }

    fun startTrack(firstTrack: Boolean = false) {
        startTracking()
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        isFirstTrack = firstTrack

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                val progressValue = when (userInfo.targetType) {
                    TargetType.TIME -> timeTrackInSeconds.value!! / 60f
                    TargetType.DISTANCE -> distanceInMeters.value!!.toFloat()
                    else -> 0f
                }
                val percentage = progressValue / userInfo.targetType.value * 100f
                isTargetReached.postValue(percentage >= 100)
                progress.postValue(percentage.toInt())

                lapTime = System.currentTimeMillis() - timeStarted
                timeTrackInMillis.postValue(timeTrack + lapTime)

                if (timeTrackInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    val lastPointIndex = pathPoints.value!!.last().lastIndex
                    if (lastPointIndex > lastCounted) {
                        var distance = 0f
                        for (i in lastCounted until lastPointIndex) {
                            val pos1 = pathPoints.value!!.last()[i]
                            val pos2 = pathPoints.value!!.last()[i + 1]

                            val result = FloatArray(1)
                            Location.distanceBetween(
                                pos1.latitude,
                                pos1.longitude,
                                pos2.latitude,
                                pos2.longitude,
                                result
                            )
                            distance += result[0]
                        }

                        lastCounted = lastPointIndex
                        val newDistance = distanceInMeters.value!! + distance.toInt()
                        distanceInMeters.postValue(newDistance)
                        caloriesBurned.postValue(((newDistance / 1000f) * userInfo.weight).toInt())
                    }

                    timeTrackInSeconds.postValue(timeTrackInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                delay(Constants.TIMER_UPDATE_INTERVAL)
            }

            timeTrack += lapTime
        }
    }

    fun pauseTrack() {
        isTracking.value = false
        isTimerEnabled = false
        lastCounted = 0
    }

    fun cancelTrack() {
        isCancelled = true
        isFirstTrack = true
        timeTrackInMillis.value = 0L // reset value for correct fragment observers income values
        distanceInMeters.value = 0
        caloriesBurned.value = 0
        progress.value = 0
        isTargetReached.value = false
        pauseTrack()
        initStartingValues()
    }

    fun addPoint(latLng: LatLng) {
        pathPoints.value?.last()?.add(latLng)
        pathPoints.postValue(pathPoints.value)
    }

    private fun addEmptyPolyline() {
        pathPoints.value?.add(mutableListOf())
    }

    companion object {
        val isTracking = MutableLiveData(false)
        val pathPoints = MutableLiveData<Polylines>()
        val timeTrackInMillis = MutableLiveData(0L)
        val timeTrackInSeconds = MutableLiveData(0L)
        val distanceInMeters = MutableLiveData(0)
        val caloriesBurned = MutableLiveData(0)
        val progress = MutableLiveData(0)
        var targetType = MutableLiveData(TargetType.NONE)
        var isTargetReached = MutableLiveData(false)
    }
}
