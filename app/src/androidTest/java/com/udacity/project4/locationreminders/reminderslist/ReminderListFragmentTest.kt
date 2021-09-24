package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.permission.PermissionRequester
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.udacity.project4.R
import com.udacity.project4.RemindersActivityTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.PermissionUtils
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.sql.DataSource

@FixMethodOrder(MethodSorters.JVM)
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {
    private val TAG = javaClass.simpleName
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var scenario: FragmentScenario<ReminderListFragment>
    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = get()



        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
    }

    @After
    fun tearDown() {
        Log.d(TAG, "tearDown")
    }


//    TODO: test the displayed data on the UI.
    @Test
    fun noDataInDb_noDataTextViewDisplayedInUi() = runBlockingTest {
        Log.d(TAG, "DB Test")
        PermissionUtils.grantPermissions()
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun clickFAB_navigateToSaveReminderFragment() = runBlockingTest {
        Log.d(TAG, "FAB Test")
        PermissionUtils.grantPermissions()
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder())
    }


    @Test
    fun locationPermissionGranted_snackBarWithRequestNotShowsUp() {
        PermissionUtils.grantPermissions()

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(doesNotExist())
    }

//    @Test
//    fun locationPermissionNotGranted_snackBarWithRequestShowsUp() {
//        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        val navController = mock(NavController::class.java)
//
//        scenario.onFragment {
//            Navigation.setViewNavController(it.view!!, navController)
//        }
//
//        onView(withId(com.google.android.material.R.id.snackbar_text))
//            .check(matches(withText(R.string.permission_denied_explanation)))
//    }
    //    TODO: add testing for the error messages.

}