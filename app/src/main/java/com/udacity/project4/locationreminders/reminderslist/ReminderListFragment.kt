package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.GlobalContext.get

@RequiresApi(Build.VERSION_CODES.M)
class ReminderListFragment : BaseFragment() {
    val TAG = "Dev/ReminderListFragment"
    private lateinit var optionsMenu: Menu
//    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
//    override val _viewModel = get<RemindersListViewModel>()

    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )

        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener {
            Log.d(TAG, "Refresh Begun")
            _viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
            Log.d(TAG, "Refresh finished")
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {

            activity?.apply {
                moveTaskToBack(true);
                finish();
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
//        checkPermissionsAndStartGeofencing()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d(TAG, "OnCreateOptionsMenu")
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)

        optionsMenu = menu
        observeAuthenticationState()
    }

    private fun observeAuthenticationState() {
        _viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                RemindersListViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        })
    }


//    private fun checkPermissionsAndStartGeofencing() {
////        if (_viewModel.geofenceIsActive()) return
//        if (foregroundAndBackgroundLocationPermissionApproved()) {
//            checkDeviceLocationSettingsAndStartGeofence()
//        } else {
//            requestForegroundAndBackgroundLocationPermissions()
//        }
//    }

//    @TargetApi(29)
//    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
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

//    @TargetApi(29 )
//    private fun requestForegroundAndBackgroundLocationPermissions() {
//
//        if (foregroundAndBackgroundLocationPermissionApproved())
//            return
//
//        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        val resultCode = when {
//            runningQOrLater -> {
//                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//            }
//            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//        }
//
//        Log.d(TAG, "Request foreground only location permission")
//        requestPermissions(
//            permissionArray,
//            resultCode
//        )
//    }

//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray): Unit {
//        Log.d(TAG, "onRequestPermissionResult")
//
//        if (grantResults.isEmpty() ||
//            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
//            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//                    && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)) {
//
//            Snackbar.make(
//                binding.root,
//                R.string.permission_denied_explanation,
//                Snackbar.LENGTH_INDEFINITE
//            )
//                .setAction(R.string.settings) {
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }.show()
//        } else {
//            checkDeviceLocationSettingsAndStartGeofence()
//        }
//    }

//    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
//
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
//                    exception.startResolutionForResult(requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON
//                    )
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
//                }
//            } else {
//                Snackbar.make(
//                    binding.root,
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettingsAndStartGeofence()
//                }.show()
//            }
//        }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, "onActivityResult")
//
//        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
//            checkDeviceLocationSettingsAndStartGeofence(false)
//        }
//    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT = "ReminderListFragment.locationreminder.action.ACTION_GEOFENCE_EVENT"
    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1