package com.example.tracking.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.tracking.db.Track
import com.example.tracking.utils.SortType
import com.example.tracking.utils.Sortable
import com.example.tracking.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(mainRepository: MainRepository) : ViewModel(), Sortable {
    override val tracksSortedByDate = mainRepository.getAllTracksSortedByDate()
    override val tracksSortedByTime = mainRepository.getAllTracksSortedByTimeInMillis()
    override val tracksSortedByDistance = mainRepository.getAllTracksSortedByDistance()
    override val tracks = MediatorLiveData<List<Track>>()
    override var sortType = SortType.DATE

    init {
        fillSources()
    }
}
