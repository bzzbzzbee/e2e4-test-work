package com.example.e2e4_test_work.fragments

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.e2e4_test_work.*
import com.example.e2e4_test_work.R.drawable.ic_marker
import com.example.e2e4_test_work.databinding.MapsFragmentBinding
import com.example.e2e4_test_work.ui.MapsFragmentViewModel
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationUpdate
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference

@AndroidEntryPoint
class MapFragment : Fragment(), PermissionsListener {

    private var _binding: MapsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapbox: MapboxMap

    private val mapsViewModel: MapsFragmentViewModel by viewModels()

    private lateinit var permissionManager: PermissionsManager

    private var locationEngine: LocationEngine? = null

    private val callback: MapsFragmentLocationCallback =
        MapsFragmentLocationCallback(this)

    private var mapLoaded = false

    private class MapsFragmentLocationCallback constructor(fragment: MapFragment?) :
        LocationEngineCallback<LocationEngineResult> {
        private val fragmentWeakReference: WeakReference<MapFragment> = WeakReference(fragment)

        override fun onSuccess(result: LocationEngineResult) {
            val fragment = fragmentWeakReference.get()
            fragment?.let {
                result.lastLocation?.let {

                    fragment.mapbox.locationComponent
                        .forceLocationUpdate(
                            LocationUpdate
                                .Builder()
                                .location(it)
                                .build()
                        )

                    val source: GeoJsonSource? =
                        fragment.mapbox.style?.getSourceAs(SOURCE_ID)

                    source?.setGeoJson(
                        FeatureCollection.fromFeatures(
                            fragment.mapsViewModel.getFeatures(it)
                        )
                    )
                }
            }
        }

        override fun onFailure(ex: Exception) {
            Log.e("LocationChangeActivity", ex.localizedMessage)
            val fragment = fragmentWeakReference.get()
            Toast.makeText(
                fragment?.requireContext(), ex.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        _binding = MapsFragmentBinding.inflate(inflater, container, false)

        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapbox = mapboxMap
            mapbox.setStyle(Style.MAPBOX_STREETS) { style ->
                setStyle(style)
                enableLocation(style)
            }
            mapLoaded = true
        }

        binding.locationFAB.setOnClickListener {
            if (isPermissionsGranted()) {
                enableLocationEngineSafe(mapbox.style!!)
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.ask_to_enable_permission,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return binding.root
    }

    private fun setStyle(style: Style) {
        style.addImage(
            ICON_ID,
            ResourcesCompat.getDrawable(
                resources,
                ic_marker,
                null
            )!!
        )

        style.addSource(GeoJsonSource(SOURCE_ID))

        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(
                    iconImage(ICON_ID),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
        )
    }

    private fun enableLocation(@NonNull style: Style) {
        if (isPermissionsGranted()) {
            initializeLocationComponent(style)
            initializeLocationEngine()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(activity)
        }
    }

    private fun enableLocationEngineSafe(@NonNull style: Style) {
        initializeLocationComponent(style)
        locationEngine?.removeLocationUpdates(callback)
        initializeLocationEngine()
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())

        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .build()

        locationEngine?.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationEngine?.getLastLocation(callback)
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationComponent(style: Style) {
        val locationComponent: LocationComponent = mapbox.locationComponent
        val locationComponentActivationOptions =
            LocationComponentActivationOptions.builder(requireContext(), style)
                .useDefaultLocationEngine(false)
                .build()

        locationComponent.activateLocationComponent(locationComponentActivationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.NORMAL

    }

    override fun onExplanationNeeded(p0: MutableList<String>?) {
        Toast.makeText(
            requireContext(),
            R.string.user_location_permission_explanation,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {

        if (granted) {
            mapbox.style?.let { enableLocation(it) }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionManager.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    private fun onResumeIfPermissionsGranted() {
        if (isPermissionsGranted() && mapLoaded) {
            enableLocationEngineSafe(mapbox.style!!)
        }
    }

    private fun isPermissionsGranted() =
        PermissionsManager.areLocationPermissionsGranted(requireActivity())

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        onResumeIfPermissionsGranted()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationEngine?.removeLocationUpdates(callback)
        mapView.onDestroy()
        _binding = null
    }
}