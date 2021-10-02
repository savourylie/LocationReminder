package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.utils.*
import java.util.*


class SelectLocationFragment() : BaseFragment(), OnMapReadyCallback {
    val TAG = "Dev/" + javaClass.simpleName

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var isPointSelected = false
//    private lateinit var poiSelected: PointOfInterest?
    private var selectedLocationStr = ""
    private var latSelected = 0.0
    private var lngSelected = 0.0

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private var deviceLocationSettingOn: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = if (selectedLocationStr == "") "(%1$.5f, %2$.5f)".format(lngSelected, latSelected) else selectedLocationStr
        _viewModel.latitude.value = latSelected
        _viewModel.longitude.value = lngSelected
        activity?.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
//        checkAndRequestPermissions()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    // Map Stuff
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        map = googleMap

        val latitude = 22.5330
        val longitude = 114.0559
        val zoomLevel = 15f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)

        enableMyLocation()
    }

    private fun setMapLongClick(map: GoogleMap) {
        fun setMarker(map: GoogleMap, latLng: LatLng): Map<String, Double> {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            return mapOf<String, Double>(
                "lat" to latLng.latitude,
                "lng" to latLng.longitude
            )
        }

        map.setOnMapLongClickListener { latLng ->
            if (isPointSelected) {
                map.clear()
                isPointSelected = false
            }

            val latLngMap = setMarker(map, latLng)
            isPointSelected = true

            selectedLocationStr = ""
            latSelected = latLngMap["lat"]!!
            lngSelected = latLngMap["lng"]!!

            onLocationSelected()

//            if (deviceLocationSettingOn == null) {
//                checkDeviceLocationSettings()
//            }
//
//            deviceLocationSettingOn?.let {
//                if (it) onLocationSelected() else checkDeviceLocationSettings()
//            }
        }
    }

    private fun setPoiClick(map: GoogleMap) {

        fun setMarker(map: GoogleMap, poi: PointOfInterest): HashMap<String, Any> {
            val poiMarker: Marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            isPointSelected = true

            return hashMapOf(
                "name" to poi.name,
                "lat" to poi.latLng.latitude,
                "lng" to poi.latLng.longitude
            )
        }

        map.setOnPoiClickListener { poi ->
            if (isPointSelected) {
                map.clear()
                isPointSelected = false
            }

            val poiMap = setMarker(map, poi)

            selectedLocationStr = (poiMap["name"] as String?)!!
            latSelected = (poiMap["lat"] as Double?)!!
            lngSelected = (poiMap["lng"] as Double?)!!

            onLocationSelected()
//            if (deviceLocationSettingOn == null) {
//                checkDeviceLocationSettings()
//            }
//
//            deviceLocationSettingOn?.let {
//                if (it) onLocationSelected() else checkDeviceLocationSettings()
//            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)

        }
    }


//    private fun isPermissionGranted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
//    }

    // Permission stuff
//    @TargetApi(29)
//    fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
//
//        val foregroundLocationApproved = (
//                PackageManager.PERMISSION_GRANTED ==
//                        ActivityCompat.checkSelfPermission(requireContext(),
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                )
//
//        val backgroundLocationApproved =
//            if (runningQOrLater) {
//                PackageManager.PERMISSION_GRANTED ==
//                        ActivityCompat.checkSelfPermission(
//                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                        )
//            } else {
//                true
//            }
//
//        return foregroundLocationApproved && backgroundLocationApproved
//    }

    fun enableMyLocation() {
        if (!foregroundLocationPermissionApproved(context!!)) {

            requestForegroundPermission(context!!, ::requestPermissions)
        } else {
            map.setMyLocationEnabled(true)
            map.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        /*
        The method is called for every call on requestPermissions(String[], Int)
         */
        Log.d(TAG, "onRequestPermissionsResult")

        if (grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
//            ||
//            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//                    && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        ) {

            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            map.setMyLocationEnabled(true)
            map.uiSettings.isMyLocationButtonEnabled = true
//            enableMyLocation()
        }
    }
}

const val LOCATION_PERMISSION_INDEX = 0
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34