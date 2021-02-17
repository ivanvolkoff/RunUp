package com.volkov.runup.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.volkov.runup.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(val mainRepository: MainRepository): ViewModel(){

    val totalTimeRun = mainRepository.getTotalTImeinMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCalories()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()


}