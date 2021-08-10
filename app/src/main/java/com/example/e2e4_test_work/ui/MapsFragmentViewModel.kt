package com.example.e2e4_test_work.ui

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.e2e4_test_work.data.MapsAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsFragmentViewModel @Inject constructor(private val repository: MapsAppRepository) : ViewModel() {
    fun getFeatures(location: Location) = repository.getFeatures(location)
}