package com.example.tracking.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.tracking.db.Track

interface Sortable {
    var sortType: SortType
    val tracks: MediatorLiveData<List<Track>>
    val tracksSortedByDate: LiveData<List<Track>>
    val tracksSortedByTime: LiveData<List<Track>>
    val tracksSortedByDistance: LiveData<List<Track>>

    fun fillSources() {
        tracks.addSource(tracksSortedByDate) {
            if (sortType == SortType.DATE) it?.let { tracks.value = it }
        }

        tracks.addSource(tracksSortedByDistance) {
            if (sortType == SortType.DISTANCE) it?.let { tracks.value = it }
        }
    }

    fun sortTracks(sortType: SortType) = when (sortType) {
        SortType.DATE -> tracksSortedByDate.value?.let { tracks.value = it }
        SortType.DISTANCE -> tracksSortedByDistance.value?.let { tracks.value = it }
    }.also { this.sortType = sortType }
}
