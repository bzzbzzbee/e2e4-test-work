package com.example.e2e4_test_work.data

data class Geometry (
    var type: String? = null,
    var coordinates: List<Int>? = null
    )

data class Properties (
    var name: String? = null
)

data class Feature (
    var type: String? = null,
    var geometry: Geometry? = null,
    var properties: Properties? = null
)

data class Root (
    var type: String? = null,
    var features: List<Feature>? = null,
)
