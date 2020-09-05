package com.example.tracking.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TrackDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("SELECT * FROM tracking_table ORDER BY timestamp DESC")
    fun getAllTracksSortedByDate(): LiveData<List<Track>>

    @Query("SELECT * FROM tracking_table ORDER BY timeInMillis DESC")
    fun getAllTracksSortedByTimeInMillis(): LiveData<List<Track>>

    @Query("SELECT * FROM tracking_table ORDER BY distanceInMeters DESC")
    fun getAllTracksSortedByDistance(): LiveData<List<Track>>

    @Query("SELECT SUM(timeInMillis) FROM tracking_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(distanceInMeters) FROM tracking_table")
    fun getTotalDistance(): LiveData<Long>
}
