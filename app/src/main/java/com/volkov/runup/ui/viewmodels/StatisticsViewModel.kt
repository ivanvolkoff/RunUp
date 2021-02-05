package com.volkov.runup.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.volkov.runup.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(val mainRepository: MainRepository): ViewModel(){

}