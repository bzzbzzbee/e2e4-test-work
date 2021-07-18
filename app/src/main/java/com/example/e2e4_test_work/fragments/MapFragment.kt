package com.example.e2e4_test_work.fragments

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.example.e2e4_test_work.BuildConfig
import com.example.e2e4_test_work.R
import com.example.e2e4_test_work.databinding.MapsFragmentBinding
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationUpdate
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference


@AndroidEntryPoint
class MapFragment : Fragment(), PermissionsListener {

    private var _binding: MapsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapbox: MapboxMap

    private lateinit var permissionManager: PermissionsManager

    private var locationEngine: LocationEngine? = null

    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private val callback: MapsFragmentLocationCallback =
        MapsFragmentLocationCallback(this)

    private class MapsFragmentLocationCallback constructor(fragment: MapFragment?) :
        LocationEngineCallback<LocationEngineResult> {
        private val fragmentWeakReference: WeakReference<MapFragment> = WeakReference(fragment)

        override fun onSuccess(result: LocationEngineResult) {
            val fragment = fragmentWeakReference.get()
            fragment?.let {
                result.lastLocation?.let {
                    Toast.makeText(
                        fragment.requireContext(),
                        String.format(
                            fragment.getString(R.string.new_location),
                            result.lastLocation?.latitude.toString(),
                            result.lastLocation?.longitude.toString()
                        ),
                        Toast.LENGTH_SHORT
                    ).show()

                    fragment.mapbox.locationComponent
                        .forceLocationUpdate(
                            LocationUpdate
                                .Builder()
                                .location(it)
                                .build()
                        )
                }
            }
        }

        override fun onFailure(ex: Exception) {
            Log.d("LocationChangeActivity", ex.localizedMessage)
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
        Mapbox.getInstance(requireContext(), BuildConfig.MapboxAccessToken)
        _binding = MapsFragmentBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapbox = mapboxMap
            mapbox.setStyle(Style.MAPBOX_STREETS) { style ->
                enableLocation(style)
            }
        }
        return binding.root
    }

    private fun enableLocation(@NonNull style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireActivity())) {
            initializeLocationComponent(style)
            initializeLocationEngine()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(activity)
        }
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

        locationComponent.apply {
            activateLocationComponent(locationComponentActivationOptions)
            isLocationComponentEnabled = true
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.NORMAL
        }
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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
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