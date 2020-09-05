package com.example.tracking.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracking.databinding.ItemTrackBinding
import com.example.tracking.db.Track

class TrackAdapter (val lister : OnItemClickListenerTrack): ListAdapter<Track, TrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position), lister)
    }

    class TrackViewHolder private constructor(
        val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Track, lister: OnItemClickListenerTrack) {
            binding.track = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                lister.onItemClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): TrackViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemTrackBinding.inflate(layoutInflater, parent, false)
                return TrackViewHolder(binding)
            }
        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Track, newItem: Track) =
            oldItem.hashCode() == newItem.hashCode()
    }

    interface OnItemClickListenerTrack {
        fun onItemClick(track: Track?)
    }
}
