package com.udacity.project4.locationreminders.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.sendNotification
import errorMessage
import sendGeofenceEnteredNotification
import android.os.Bundle

import android.content.Intent.getIntent
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem


/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    val TAG = "Dev/" + javaClass.simpleName

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        Log.d(TAG, intent.action!!)

        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)

                return
            }

            Log.d(TAG, geofencingEvent.geofenceTransition.toString())

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))
                val args = intent.getBundleExtra("data")
                val reminder: ReminderDataItem = args!!.getSerializable("reminderDataItem") as ReminderDataItem

                sendNotification(context, reminder)

                val fenceId = when {
                        geofencingEvent.triggeringGeofences.isNotEmpty() -> {
                            Log.v(TAG, "Geofence Trigger Found!")
                            geofencingEvent.triggeringGeofences[0].requestId
                        }
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }
            }

//            val notificationManager = ContextCompat.getSystemService(
//                context,
//                NotificationManager::class.java
//            ) as NotificationManager

//            sendNotification(context, reminder)
//            notificationManager.sendGeofenceEnteredNotification(
//                context, 1
//            )
        }
    }
}

private const val TAG = "GeofenceReceiver"

