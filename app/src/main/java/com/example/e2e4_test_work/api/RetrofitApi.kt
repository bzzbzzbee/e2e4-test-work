package com.example.e2e4_test_work.api

import com.example.e2e4_test_work.data.Root
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitApi {
    @GET
    suspend fun loadResponse(@Url url: String): Response<Root>
}