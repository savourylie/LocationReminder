package com.udacity.project4.locationreminders.savereminder

import GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import android.Manifest
import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.LOCATION_PERMISSION_INDEX
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.utils.*
import com.udacity.project4.utils.requestForegroundPermission
import org.koin.android.ext.android.inject
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.M)
class SaveReminderFragment : BaseFragment() {
    val TAG = "Dev/" + javaClass.simpleName

    //Get the view model this time as a single to be shared with the another fragment
    private lateinit var contxt: Context
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    // Geofencing stuff
    private lateinit var geofencingClient: GeofencingClient
    private var deviceLocationSettingOn = MutableLiveData<Boolean>()
    private lateinit var geofencePendingIntent: PendingIntent

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel

        // Geofencing
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminderItem =  ReminderDataItem(
            title = title,
            description = description,
            location = location,
            latitude = latitude,
            longitude = longitude
        )

        geofencePendingIntent = createGeofencePendingIntent(reminderItem)
//        addGeofence(reminderItem)

        return binding.root
    }

    private fun createGeofencePendingIntent(reminderItem: ReminderDataItem): PendingIntent {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ReminderListFragment.ACTION_GEOFENCE_EVENT
        val args = Bundle()
        args.putSerializable("reminderDataItem", reminderItem)
        intent.putExtra("data", args)
//                geofencePendingIntent = PendingIntent.getBroadcast(MyApp.context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        geofencePendingIntent = PendingIntent.getBroadcast(MyApp.context, Random.nextInt(0, 1000), intent, PendingIntent.FLAG_ONE_SHOT)

        return geofencePendingIntent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            // 1. Check permissions are on (make requests and deal with callbacks)
            // 2. Check Location setting is on (make requests)
            // 3. SaveReminder (triggered by livedata)
            // 4. Add geofence (triggered by livedata)
            checkPermissionsAndStartGeofencing()
//            enableForegroundAndBackgroundPermissions()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun addGeofence() {

        if (_viewModel.validateEnteredData()) {
            val geofence = Geofence.Builder()
                .setRequestId(Random.nextInt(0, 1000).toString()) // Set request id
                .setCircularRegion(_viewModel.latitude.value!!, // Set geofence location
                    _viewModel.longitude.value!!,
                    GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE) // Set expiration
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER) // Set transition type
                .build()

            val geofencingRequest = GeofencingRequest.Builder() // Build request
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Log.d(TAG, "addOnSuccessListener")
                    Toast.makeText(requireContext(),
                        R.string.geofences_added,
                        Toast.LENGTH_SHORT)
                        .show()
                    Log.e("Add Geofence", geofence.requestId)

                    _viewModel.validateAndSaveReminder()
                    _viewModel.navigationCommand.value = NavigationCommand.Back

                }

                addOnFailureListener { e ->
                    Toast.makeText(context, R.string.geofences_not_added,
                        Toast.LENGTH_SHORT).show()
                    if (e.message != null) {
                        Log.w(TAG, e.message!!)
                    }
                    Log.d(TAG, "Geofence not added.")

                    _viewModel.navigationCommand.value = NavigationCommand.Back
                }
            }
        }

//        else {
//            Toast.makeText(context, "Reminder not saved due to invalid input.", Toast.LENGTH_SHORT).show()
//        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")
        contxt = context
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {

        Log.d(TAG, "onRequestPermissionsResult")

        if (grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
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
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved(context!!)) {
            checkDeviceLocationSettingsAndStartGeofence(true)
        } else {
            requestForegroundAndBackgroundLocationPermissions(context!!, ::requestPermissions)
        }
    }

//    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
//        val locationRequest = LocationRequest.create().apply {
//            priority = LocationRequest.PRIORITY_LOW_POWER
//        }
//
//        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
//        val settingsClient = LocationServices.getSettingsClient(requireActivity())
//        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
//
//        locationSettingsResponseTask.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException && resolve) {
//                try {
//                    exception.startResolutionForResult(
//                        requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON
//                    )
//
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
//                }
//            } else {
//                Snackbar.make(
//                    binding.root,
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettings()
//                }.show()
//            }
//        }
//
//        locationSettingsResponseTask.addOnCompleteListener {
//            if (it.isSuccessful) {
//                Log.d(TAG, "Device location is on.")
//
//                addGeofence()
//            }
//        }
//    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val locationSettingRequestsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(locationSettingRequestsBuilder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->

            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    resultLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    this.requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Log.d(TAG, "Device location enabled")
                addGeofence()
            }
        }
    }
}



