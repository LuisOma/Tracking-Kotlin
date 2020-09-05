package com.example.tracking.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracking.R
import com.example.tracking.ui.adapters.TrackAdapter
import com.example.tracking.databinding.FragmentTracksBinding
import com.example.tracking.db.Track
import com.example.tracking.utils.TrackingUtility
import com.example.tracking.ui.DetailTrackingActivity
import com.example.tracking.ui.viewmodels.TrackingViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracks.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream


@AndroidEntryPoint
class TracksFragment : Fragment(R.layout.fragment_tracks), EasyPermissions.PermissionCallbacks, TrackAdapter.OnItemClickListenerTrack, View.OnClickListener {
    private var _binding: FragmentTracksBinding? = null
    private val binding get() = _binding!!
    private val trackingViewModel: TrackingViewModel by viewModels()
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTracksBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = trackingViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TrackingUtility.requestPermissions(this)
        setupRecyclerView()
        trackingViewModel.tracks.observe(viewLifecycleOwner, Observer {
            trackAdapter.submitList(it)
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fabScroll.setOnClickListener(this)
        rvTracks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    fabScroll.show()
                } else {
                    fabScroll.hide()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvTracks.apply {
            trackAdapter = TrackAdapter(this@TracksFragment)
            adapter = trackAdapter
            layoutManager = LinearLayoutManager(requireContext())
            ItemTouchHelper(SwipeToDeleteCallback()).attachToRecyclerView(this)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            TrackingUtility.requestPermissions(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        private val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_64)
        private val background = ColorDrawable(Color.RED)
        private var deletedItem: Track? = null
        private var deletedItemPosition = 0

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val list = trackAdapter.currentList.toMutableList()
            deletedItem = list[position]
            deletedItemPosition = position
            list.removeAt(position)
            trackAdapter.submitList(list)
            trackingViewModel.deleteTrack(deletedItem!!)
            showUndo()
        }

        private fun showUndo() {
            Snackbar.make(
                requireView(),
                requireContext().getString(R.string.track_deleted),
                Snackbar.LENGTH_LONG
            ).run {
                setAction(R.string.undo) { undoDeletion() }
                setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                show()
            }
        }

        private fun undoDeletion() {
            val list = trackAdapter.currentList.toMutableList()
            list.add(deletedItemPosition, deletedItem)
            trackAdapter.submitList(list)
            trackingViewModel.restoreDeletedTrack(deletedItem!!)
            Snackbar.make(
                requireView(),
                getString(R.string.track_restored),
                Snackbar.LENGTH_SHORT
            ).show()
            deletedItem = null
            deletedItemPosition = 0
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val itemView = viewHolder.itemView
            val backgroundCornerOffset = 20

            val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
            val iconBottom = iconTop + icon.intrinsicHeight

            when {
                dX > 0 -> {
                    val iconLeft = itemView.left
                    val iconRight = itemView.left + icon.intrinsicWidth * 3
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset,
                        itemView.bottom
                    )
                }
                dX < 0 -> {
                    val iconLeft = (itemView.width - icon.intrinsicWidth)
                    val iconRight = itemView.right + icon.intrinsicWidth * 2
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(
                        (itemView.right + dX).toInt() - backgroundCornerOffset,
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                }
                else -> {
                    background.setBounds(0, 0, 0, 0)
                }
            }
            background.draw(c)
            icon.draw(c)
        }
    }

    @SuppressLint("ResourceType")
    override fun onItemClick(track: Track?) {
        activity?.let{
            val stream = ByteArrayOutputStream()
            track!!.image?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            val intent = Intent (it, DetailTrackingActivity::class.java)
            intent.putExtra("image", byteArray)
            intent.putExtra("name", track.routeName)
            intent.putExtra("time", track.timeInMillis)
            intent.putExtra("distance", track.distanceInMeters)
            it.startActivity(intent)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fabScroll -> {
                rvTracks.post {
                    rvTracks.smoothScrollToPosition(0)
                }
            }
        }
    }
}
