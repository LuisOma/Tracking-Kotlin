package com.example.tracking.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracking_table")
data class Track(
    var image: Bitmap? = null,
    var timestamp: Long = 0L,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var routeName: String? = null
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
