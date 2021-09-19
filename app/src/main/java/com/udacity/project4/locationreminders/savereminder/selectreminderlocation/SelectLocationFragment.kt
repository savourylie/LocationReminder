package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import java.util.*


class SelectLocationFragment() : BaseFragment(), OnMapReadyCallback {

    val TAG = "Dev/SelectLocationFragment"
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var isPoiSelected = false
    private lateinit var poiSelected: PointOfInterest
    private val REQUEST_LOCATION_PERMISSION = 1

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

//        TODO: add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
//        onLocationSelected()

        _viewModel.selectedPOI.observe(viewLifecycleOwner, Observer { newPoi ->
            newPoi?.let {
                _viewModel.reminderSelectedLocationStr.value = it.name
            }
        })

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        binding.viewModel?.selectedPOI?.value = poiSelected
        activity?.onBackPressed()
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val latitude = 22.527141
        val longitude = 114.050752
        val zoomLevel = 18f
//        val overlaySize = 100f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
//        locationRequest = LocationRequest()
//        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
//        map.isMyLocationEnabled = true

        // Add little green bot image
//        val androidOverlay = GroundOverlayOptions()
//            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
//            .position(homeLatLng, overlaySize)
//        map.addGroundOverlay((androidOverlay))
//        setMapLongClick(map)
        setPoiClick(map)
//        setMapStyle(map)
        enableMyLocation()

    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.map_options, menu)
//        return true
//    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
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
        }
    }

    private fun setPoiClick(map: GoogleMap) {

        fun setMarker(map: GoogleMap, poi: PointOfInterest) {
            val poiMarker: Marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            isPoiSelected = true
        }

        map.setOnPoiClickListener { poi ->
            if (!isPoiSelected) {
                setMarker(map, poi)

            } else {
                map.clear()
                isPoiSelected = false
                setMarker(map, poi)
            }

            poiSelected = poi
            Log.d(TAG, poiSelected.name)
            Log.d(TAG, poiSelected.latLng.toString())
            onLocationSelected()
        }
    }

//    private fun setMapStyle(map: GoogleMap) {
//        try {
//            val success = map.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(
//                    getContext(),
//                    R.raw.map_style
//                )
//            )
//
//            if (!success) {
//                Log.e(TAG, "Style parsing failed.")
//            }
//        } catch (e: Resources.NotFoundException) {
//            Log.e(TAG, "Can't find style. Error: ", e)
//
//        }
//    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

//    private var locationCallback: LocationCallback = object: LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            val locationList = locationResult.locations
//            if (locationList.isNotEmpty()) {
//                //The last location in the list is the newest
//                val location = locationList.last()
//                Log.i(
//                    "MapsActivity",
//                    "Location: " + location.getLatitude() + " " + location.getLongitude()
//                )
//
//                //Place current location marker
//                val latLng = LatLng(location.latitude, location.longitude)
//
//                //move map camera
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11.0F))
//            }
//        }
//    }
}
