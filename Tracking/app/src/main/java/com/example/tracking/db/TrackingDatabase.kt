package com.example.tracking.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Track::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TrackingDatabase : RoomDatabase() {

    abstract fun getTrackDao(): TrackDAO
}
