package com.example.e2e4_test_work.data

import android.location.Location
import com.example.e2e4_test_work.api.DummyApi
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsAppRepository @Inject constructor(private val api: DummyApi) {

    companion object {
        const val maxPoints = 20
        const val areaOfSearch = 10000
    }

    private var features: MutableList<Point> = mutableListOf()


    fun getFeatures(userLocation: Location): List<Feature> {
        if (features.isEmpty()) {
            val generatedFeatures = api.getFutures(userLocation).map { it.geometry() as Point }

            features = generatedFeatures.filter { isValidPoint(userLocation, it, areaOfSearch) }
                .take(maxPoints).toMutableList()
        } else {
            features =
                features.filter { isValidPoint(userLocation, it, areaOfSearch) }.toMutableList()

            val generatedPoints = api.getFutures(userLocation).map { it.geometry() as Point }

            val filteredApiResponsePoints = generatedPoints.filter {
                isValidPoint(
                    userLocation,
                    it,
                    areaOfSearch
                ) && it !in features
            }.take(
                (maxPoints - features.size).zeroOrPositive()
            )

            if (filteredApiResponsePoints.isNotEmpty()) {
                features.addAll(filteredApiResponsePoints)
            }
        }
        return features.map { Feature.fromGeometry(it) }
    }

    private fun isValidPoint(userLocation: Location, pointLocation: Point, area: Int): Boolean {
        val loc = Location("Location").apply {
            longitude = pointLocation.longitude()
            latitude = pointLocation.latitude()
        }

        return userLocation.distanceTo(loc) <= area
    }
}

fun Int.zeroOrPositive(): Int {
    return if (this >= 0)
        this else 0
}