package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat


private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 35
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1


@TargetApi(29)
fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
    return foregroundLocationPermissionApproved(context) && backgroundLocationPermissionApproved(context)
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


@TargetApi(29)
fun foregroundLocationPermissionApproved(context: Context): Boolean {

    return (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
            )
}


@TargetApi(29)
fun backgroundLocationPermissionApproved(context: Context): Boolean {

    return if (runningQOrLater) {
        PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    } else {
        true
    }
}


@TargetApi(29 )
fun requestForegroundPermission(context: Context,
                                requestPermissions: (Array<String>, Int) -> Unit): Unit {

    if (foregroundLocationPermissionApproved(context))
        return

    val permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

    requestPermissions(permissionArray, resultCode)
}

@TargetApi(29 )
fun requestBackgroundPermission(context: Context,
                                requestPermissions: (Array<String>, Int) -> Unit): Unit {

    if (backgroundLocationPermissionApproved(context))
        return

    val permissionArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    val resultCode = REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE

    requestPermissions(permissionArray, resultCode)
}
