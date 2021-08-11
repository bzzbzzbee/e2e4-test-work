package com.example.e2e4_test_work.api

import android.location.Location
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DummyApi {

    companion object {
        //1 градус примерно равен 111 км
        const val tenKmDouble: Double = 0.3
    }

    private fun generateLocations(lonOrLat: Double) =
        Random.nextDouble(lonOrLat - tenKmDouble, lonOrLat + tenKmDouble)

    private var features: MutableList<Feature> = mutableListOf()

    fun getFutures(location: Location): MutableList<Feature> {
        if (features.isEmpty()) {
            repeat(50) {
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