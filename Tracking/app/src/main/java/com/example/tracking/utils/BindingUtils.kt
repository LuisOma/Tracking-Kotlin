package com.example.tracking.utils

import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.tracking.R
import com.example.tracking.db.Track
import com.example.tracking.repositories.TrackingRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

@BindingAdapter("loadImage")
fun ImageView.loadImage(track: Track) {
    Glide.with(this).load(track.image).into(this)
}

@BindingAdapter("setName")
fun MaterialTextView.setName(track: Track) {
    val name = track.routeName
    text = name
}

@BindingAdapter("setDistanceInKm")
fun MaterialTextView.setDistanceInKm(track: Track) {
    val km = track.distanceInMeters / 1000f
    val totalDistance = (km * 10) / 10f
    text = context.getString(R.string.distance_binding_format, totalDistance)
}

@BindingAdapter("toggleTrackText")
fun setStartButtonText(button: MaterialButton, isTracking: Boolean) {
    val millis = TrackingRepository.timeTrackInMillis.value!!
    button.text =
        if (millis == 0L && !isTracking) button.context.getString(R.string.start)
        else if (millis > 0L && !isTracking) button.context.getString(R.string.resume)
        else button.context.getString(R.string.pause)
}

@BindingAdapter("setSelection")
fun Spinner.setSelection(viewModel: Sortable) {
    setSelection(viewModel.sortType.ordinal)
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            viewModel.sortTracks(SortType.values()[position])
        }
    }
}

@BindingAdapter("currentDistance")
fun MaterialTextView.currentDistance(distance: Long) {
    text = context.getString(R.string.distance_binding_format_meters, distance)
}

@BindingAdapter("updateProgress")
fun ProgressBar.updateProgress(progress: Int) {
    this.progress = progress
}
