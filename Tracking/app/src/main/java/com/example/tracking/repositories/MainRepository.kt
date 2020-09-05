package com.example.tracking.repositories

import com.example.tracking.db.Track
import com.example.tracking.db.TrackDAO
import javax.inject.Inject

class MainRepository @Inject constructor(private val trackDAO: TrackDAO) {

    suspend fun insertTrack(track : Track) = trackDAO.insertTrack(track)

    suspend fun deleteTrack(track : Track) = trackDAO.deleteTrack(track)

    fun getAllTracksSortedByDate() = trackDAO.getAllTracksSortedByDate()

    fun getAllTracksSortedByTimeInMillis() = trackDAO.getAllTracksSortedByTimeInMillis()

    fun getAllTracksSortedByDistance() = trackDAO.getAllTracksSortedByDistance()
}
