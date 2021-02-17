package com.volkov.runup.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volkov.runup.db.Run
import com.volkov.runup.other.SortType
import com.volkov.runup.repositories.MainRepository
import kotlinx.coroutines.launch


class MainViewModel @ViewModelInject constructor(val mainRepository: MainRepository) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()

    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATA

    init {
        runs.addSource(runsSortedByDate) { result ->
            if (sortType == SortType.DATA) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByAvgSpeed) { result ->
            if (sortType == SortType.SPEED) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALLORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNINGTIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRunds(sortType: SortType) = when (sortType) {
        SortType.RUNNINGTIME -> runsSortedByTimeInMillis.value?.let { runs.value == it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value == it }
        SortType.CALLORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value == it }
        SortType.SPEED -> runsSortedByAvgSpeed.value?.let { runs.value == it }
        SortType.DATA -> runsSortedByDate.value?.let { runs.value == it }

    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}