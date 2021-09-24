package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.PermissionUtils
import com.udacity.project4.util.ToastMatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject

@FixMethodOrder(MethodSorters.JVM)
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest: AutoCloseKoinTest() {
    private val TAG = javaClass.simpleName
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var scenario: FragmentScenario<SaveReminderFragment>

    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()

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

        // Get viewModel
        viewModel = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
    }

    @After
    fun tearDown() {
        Log.d(TAG, "tearDown")
    }

    @Test
    fun whenLocationNotSelected_popUpToast() = runBlockingTest {

        PermissionUtils.grantPermissions()

        onView(withId(R.id.reminderTitle))
            .perform(typeText("Title"))
            .perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription))
            .perform(typeText("Description"))
            .perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.saveReminder))
            .perform(click())
        onView(withText("Reminder not saved due to invalid input."))
            .inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
    }
}