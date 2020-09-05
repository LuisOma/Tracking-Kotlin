package com.example.tracking.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tracking.R
import com.example.tracking.databinding.FragmentTrackingBinding
import com.example.tracking.utils.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.example.tracking.utils.Constants.MAP_ZOOM
import com.example.tracking.utils.Constants.POLYLINE_COLOR
import com.example.tracking.utils.Constants.POLYLINE_WIDTH
import com.example.tracking.utils.MapLifecycleObserver
import com.example.tracking.utils.TrackingUtility
import com.example.tracking.repositories.TrackingRepository.Companion.pathPoints
import com.example.tracking.ui.viewmodels.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_layout.view.*
import timber.log.Timber

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private val trackingViewModel: TrackingViewModel by viewModels()
    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private lateinit var mapLifecycleObserver: MapLifecycleObserver
    private var menu: Menu? = null
    private lateinit var motionLayout: MotionLayout
    private val args: TrackingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        motionLayout = binding.root.findViewById(R.id.clInnerLayout)!!

        setHasOptionsMenu(true)

        mapLifecycleObserver = MapLifecycleObserver(mapView, lifecycle)

        subscribeToObservers()

        binding.apply {
            lifecycleOwner = this@TrackingFragment
            viewModel = trackingViewModel

            btnToggleTrack.setOnClickListener {
                if (trackingViewModel.currentTimeInMillis.value!! == 0L) {
                    trackingViewModel.sendCommandToService(requireContext())
                    motionLayout.transitionToEnd()
                } else {
                    trackingViewModel.sendCommandToService(requireContext())
                }
            }

            btnFinishTrack.setOnClickListener { finishTrack() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG
            ) as CancelTrackingDialog?
            cancelTrackingDialog?.setPositiveButtonListener {
                trackingViewModel.setCancelCommand(requireContext())
            }
        }

        mapView?.let { mapView ->
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync {
                map = it
                addAllPolylines()
                if (args.isFinishActionFired) {
                    finishTrack()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (trackingViewModel.isTracking.value!! || trackingViewModel.currentTimeInMillis.value!! > 0L) {
            motionLayout.transitionToEnd()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.getItem(0).isVisible = trackingViewModel.currentTimeInMillis.value!! > 0L
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.cancel_track -> {
            showCancelTrackingDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setPositiveButtonListener {
                trackingViewModel.setCancelCommand(requireContext())
                motionLayout.transitionToStart()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun subscribeToObservers() {
        trackingViewModel.isTracking.observe(viewLifecycleOwner, Observer {
            menu?.getItem(0)?.isVisible = it || trackingViewModel.currentTimeInMillis.value!! > 0L
        })

        trackingViewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            addLatestPolyline()
            moveCameraToUser()
        })

        trackingViewModel.currentTimeInMillis.observe(viewLifecycleOwner, Observer {
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun moveCameraToUser() {
        if (pathPoints.value!!.isNotEmpty() && pathPoints.value!!.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.value!!.last().last(), MAP_ZOOM)
            )
        }
    }

    private fun addAllPolylines() {
        pathPoints.value?.forEach {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)
            map?.addPolyline(polylineOptions)

        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints.value!!) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        val latLngBounds = try {
            bounds.build()
        } catch (e: IllegalStateException) {
            Timber.e(e, "No se encuentran puntos asociados a la ruta")
            return
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                mapView!!.width,
                mapView!!.height,
                (mapView!!.height * 0.05f).toInt()
            )
        )
    }

    private fun finishTrack() {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Guardar Ruta")
        val view = layoutInflater.inflate(R.layout.alert_layout, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        view.btnSave.setOnClickListener {
            val routeName = view.etRouteName.text.toString().trim()
            saveTrack(routeName)
            alertDialog.hide()
        }
    }

    private fun saveTrack(routeName: String) {
        zoomToSeeWholeTrack()
        map!!.snapshot {
            trackingViewModel.processTrack(requireContext(), it, routeName)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                getString(R.string.track_saved_successfully),
                Snackbar.LENGTH_LONG
            ).show()
        }
        findNavController().navigate(R.id.action_trackingFragment_to_runsFragment)
    }

    private fun addLatestPolyline() {
        if (pathPoints.value!!.isNotEmpty() && pathPoints.value!!.last().size > 1) {
            val preLastLatLng = pathPoints.value!!.last()[pathPoints.value!!.last().size - 2]
            val lastLatLng = pathPoints.value!!.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
