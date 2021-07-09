package com.example.e2e4_test_work.data

data class Response(
    val longitude: Long,
    val latitude: Long
)

data class Root(
    val response: List<Response>
)