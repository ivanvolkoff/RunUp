package com.volkov.runup.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.volkov.runup.R
import com.volkov.runup.db.Run
import com.volkov.runup.other.Constants.ACTION_PAUSE_SERVICE
import com.volkov.runup.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.volkov.runup.other.Constants.ACTION_STOP_SERVICE
import com.volkov.runup.other.Constants.MAP_ZOOM
import com.volkov.runup.other.Constants.POLYLINE_COLOR
import com.volkov.runup.other.Constants.POLYLINE_WIDTH
import com.volkov.runup.other.TrackingUtility
import com.volkov.runup.services.Polyline
import com.volkov.runup.services.TrackingService
import com.volkov.runup.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round
const val CANCEL_TRACKING_DIALOG_TAG = "Cancel dialog"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var menu: Menu? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L
    private var map: GoogleMap? = null

    @set:Inject
    private var weight = 75f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L) {
            this.menu?.get(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancellTracking ->
                showCancellTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancellTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        if(savedInstanceState != null){
            (parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG)as CancelTrackingDialog?)?.setYesListener { stopRun() }
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDB()
        }

        mapView?.getMapAsync {
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), MAP_ZOOM)
            )
        }

    }

    private fun zoomToSeeWholeTrack() {
        Timber.i("in zoom to see whole track")
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDB() {
        Timber.i("in end run and save  track")
        map?.snapshot { bmp ->
            var distanceInMeters = 0

            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(
                bmp,
                dateTimestamp,
                avgSpeed,
                distanceInMeters,
                currentTimeInMillis,
                caloriesBurned
            )
            viewModel.insertRun(run)
            Timber.e(run.calloriesBurned.toString())
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved succesfully",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()
        }

    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //draving all polyline like one piece polyline
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polulineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polulineOptions)
        }
    }

    // add latest polyline to list and drawing line between last and prelast LatLng
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val prelastLatLong = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polulineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(prelastLatLong)
                .add(lastLatLng)
            map?.addPolyline(polulineOptions)

        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }


}