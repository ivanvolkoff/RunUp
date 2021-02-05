package com.volkov.runup.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.volkov.runup.R
import com.volkov.runup.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment :Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()
}