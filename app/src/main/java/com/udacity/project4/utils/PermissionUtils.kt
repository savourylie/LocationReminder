package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.MyApp.Companion.context


private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

@TargetApi(29)
fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {

    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION)
            )

    val backgroundLocationApproved =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    return foregroundLocationApproved && backgroundLocationApproved
}


@TargetApi(29 )
fun requestForegroundAndBackgroundLocationPermissions(context: Context,
    requestPermissions: (Array<String>, Int) -> Unit): Unit {

    if (foregroundAndBackgroundLocationPermissionApproved(context))
        return

    var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = when {
        runningQOrLater -> {
            permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    }

    requestPermissions(permissionArray, resultCode)
}




