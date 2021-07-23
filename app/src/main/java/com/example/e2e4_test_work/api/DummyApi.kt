package com.example.e2e4_test_work.api

import android.location.Location
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DummyApi {

    //0.1 градус что примерно равнол 11.1 км
    private val tenKmDouble = 0.0174533

    private fun generateLocations(lonOrLat: Double) =
        Random.nextDouble(lonOrLat - tenKmDouble, lonOrLat + tenKmDouble)

    private var features: MutableList<Feature> = mutableListOf()

    fun getFutures(location: Location): MutableList<Feature> {
        if (features.isEmpty()) {
            repeat(10) {
                features.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            generateLocations(location.longitude),
                            generateLocations(location.latitude)
                        )
                    )
                )
            }
        }
        return features
    }
}