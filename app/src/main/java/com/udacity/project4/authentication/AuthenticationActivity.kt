package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.MyApp
import com.udacity.project4.MyApp.Companion.context
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Dev/AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1113
    }

    val _viewModel: RemindersListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        val binding = ActivityAuthenticationBinding.inflate(layoutInflater)

        binding.loginButton.setOnClickListener {
            launchSignInFlow()
        }

        setContentView(binding.root)

        observeAuthenticationState()

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :

//        finish()
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        // PS: These authentication methods have to be enabled in the Firebase console first
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun observeAuthenticationState() {
        _viewModel.authenticationState.observe(this, Observer { authenticationState ->
//        MyApp.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                RemindersListViewModel.AuthenticationState.AUTHENTICATED -> {
//                MyApp.AuthenticationState.AUTHENTICATED -> {
                    val intent = Intent(context, RemindersActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    Log.d(TAG, "User not logged in.")
                }
            }
        })
    }
}
