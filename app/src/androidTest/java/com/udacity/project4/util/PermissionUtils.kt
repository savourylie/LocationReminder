package com.udacity.project4.util

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.permission.PermissionRequester
import com.udacity.project4.MyApp

class PermissionUtils {
    companion object {
        fun grantPermissions() {
            PermissionRequester().apply {

                addPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    addPermissions(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                requestPermissions()
            }
        }

        fun revokePermissions() {
            // revoke the foreground location permission
            InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("pm revoke ${MyApp.context.packageName} android.permission.ACCESS_FINE_LOCATION")

            // revoke the background location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                InstrumentationRegistry
                    .getInstrumentation()
                    .uiAutomation
                    .executeShellCommand("pm revoke ${MyApp.context.packageName} android.permission.ACCESS_BACKGROUND_LOCATION")
            }
        }
    }
}