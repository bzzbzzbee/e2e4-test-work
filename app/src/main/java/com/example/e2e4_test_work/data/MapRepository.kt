package com.example.e2e4_test_work.data

import com.example.e2e4_test_work.api.RetrofitApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepository @Inject constructor(private val retrofitApi: RetrofitApi) {

    suspend fun getMarkers(){}
}