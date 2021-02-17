package com.volkov.runup.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.volkov.runup.R
import com.volkov.runup.other.CustomMarkerView
import com.volkov.runup.other.TrackingUtility
import com.volkov.runup.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setUpBarChart()
    }

    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalDistance = it / 1000f
                val totalDist = round(totalDistance * 10f) / 10f
                tvTotalDistance.text = totalDist.toString() + "km"
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val averageSpeedString = avgSpeed.toString() + "km/h"
                tvAverageSpeed.text = averageSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kCal"
                tvTotalCalories.text = totalCalories
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(),it[i].avgSpeedInKMH) }
                val bardataSet = BarDataSet(allAvgSpeeds,"Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart.data = BarData(bardataSet)
                barChart.marker = CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                barChart.invalidate()

            }
        })


    }

    private fun setUpBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)

            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }
}