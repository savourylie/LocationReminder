import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import java.util.concurrent.TimeUnit

//package com.udacity.project4.locationreminders.geofence
//
//import android.content.Context
//import com.google.android.gms.location.GeofenceStatusCodes
//import com.google.android.gms.maps.model.LatLng
//import java.util.concurrent.TimeUnit
//
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}
//
///**
// * Stores latitude and longitude information along with a hint to help user find the location.
// */
//data class LandmarkDataObject(val id: String, val hint: Int, val name: Int, val latLong: LatLng)
//
//internal object GeofencingConstants {
//
//    /**
//     * Used to set an expiration time for a geofence. After this amount of time, Location services
//     * stops tracking the geofence. For this sample, geofences expire after one hour.
//     */
//    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
//
//    val LANDMARK_DATA = arrayOf(
//        LandmarkDataObject(
//            "golden_gate_bridge",
//            R.string.golden_gate_bridge_hint,
//            R.string.golden_gate_bridge_location,
//            LatLng(37.819927, -122.478256)),
//
//        LandmarkDataObject(
//            "ferry_building",
//            R.string.ferry_building_hint,
//            R.string.ferry_building_location,
//            LatLng(37.795490, -122.394276)),
//
//        LandmarkDataObject(
//            "pier_39",
//            R.string.pier_39_hint,
//            R.string.pier_39_location,
//            LatLng(37.808674, -122.409821)),
//
//        LandmarkDataObject(
//            "union_square",
//            R.string.union_square_hint,
//            R.string.union_square_location,
//            LatLng(37.788151, -122.407570))
//    )
//
//    val NUM_LANDMARKS = LANDMARK_DATA.size
//    const val GEOFENCE_RADIUS_IN_METERS = 100f
//    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
//}

data class LandmarkDataObject(val id: String, val hint: Int, val name: Int, val latLong: LatLng)

object GeofencingConstants {

    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    val LANDMARK_DATA = arrayOf(
        LandmarkDataObject(
            "golden_gate_bridge",
            R.string.golden_gate_bridge_hint,
            R.string.golden_gate_bridge_location,
            LatLng(37.819927, -122.478256)
        ),

        LandmarkDataObject(
            "ferry_building",
            R.string.ferry_building_hint,
            R.string.ferry_building_location,
            LatLng(37.795490, -122.394276)
        ),

        LandmarkDataObject(
            "pier_39",
            R.string.pier_39_hint,
            R.string.pier_39_location,
            LatLng(37.808674, -122.409821)
        ),

        LandmarkDataObject(
            "union_square",
            R.string.union_square_hint,
            R.string.union_square_location,
            LatLng(37.788151, -122.407570)
        )
    )

    val NUM_LANDMARKS = LANDMARK_DATA.size
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}

fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),

            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = context.getString(R.string.notification_channel_description)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

/*
 * A Kotlin extension function for AndroidX's NotificationCompat that sends our Geofence
 * entered notification.  It sends a custom notification based on the name string associated
 * with the LANDMARK_DATA from GeofencingConstatns in the GeofenceUtils file.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun NotificationManager.sendGeofenceEnteredNotification(context: Context, foundIndex: Int) {
    val contentIntent = Intent(context, RemindersActivity::class.java)
    contentIntent.putExtra(GeofencingConstants.EXTRA_GEOFENCE_INDEX, foundIndex)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
//        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val mapImage = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.map_small
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(mapImage)
        .bigLargeIcon(null)

    // We use the name resource ID from the LANDMARK_DATA along with content_text to create
    // a custom message when a Geofence triggers.
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(context.getString(
            R.string.content_text,
            context.getString(GeofencingConstants.LANDMARK_DATA[foundIndex].name)))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .setSmallIcon(R.drawable.map_small)
        .setStyle(bigPicStyle)
        .setLargeIcon(mapImage)

    notify(NOTIFICATION_ID, builder.build())
}

private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"