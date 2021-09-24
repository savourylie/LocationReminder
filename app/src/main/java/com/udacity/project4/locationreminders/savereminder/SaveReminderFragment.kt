package com.udacity.project4.locationreminders.savereminder

import GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import android.Manifest
import android.annotation.TargetApi
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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
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
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.M)
class SaveReminderFragment : BaseFragment() {
    val TAG = "Dev/" + javaClass.simpleName

    //Get the view model this time as a single to be shared with the another fragment
    private lateinit var contxt: Context
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    // Geofencing stuff
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private lateinit var geofencePendingIntent: PendingIntent

//    private val geofencePendingIntent: PendingIntent by lazy {
//        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
//        intent.action = ReminderListFragment.ACTION_GEOFENCE_EVENT
//        PendingIntent.getBroadcast(MyApp.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//    }

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

        return binding.root
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

            Log.d(TAG, reminderItem.toString())
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db derItem)
            val isSaved = _viewModel.validateAndSaveReminder(reminderItem)

            if (!isSaved) {
                Toast.makeText(context, "Reminder not saved due to invalid input.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
                intent.action = ReminderListFragment.ACTION_GEOFENCE_EVENT
                val args = Bundle()
                args.putSerializable("reminderDataItem", reminderItem)
                intent.putExtra("data", args)
                geofencePendingIntent = PendingIntent.getBroadcast(MyApp.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                addGeofence(reminderItem)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun addGeofence(reminder: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(reminder.latitude!!,
                reminder.longitude!!,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Log.d(TAG, "addOnSuccessListener")
                        Toast.makeText(context, R.string.geofences_added,
                            Toast.LENGTH_SHORT)
                            .show()
                        Log.e("Add Geofence", geofence.requestId)

                        _viewModel.navigationCommand.value = NavigationCommand.Back
                    }
                    addOnFailureListener {
                        Toast.makeText(context, R.string.geofences_not_added,
                            Toast.LENGTH_SHORT).show()
                        if (it.message != null) {
                            Log.w(TAG, it.message!!)
                        }
                        _viewModel.saveReminder(reminder)
                        _viewModel.navigationCommand.value = NavigationCommand.Back
                    }
                }
            }
        }
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


}



