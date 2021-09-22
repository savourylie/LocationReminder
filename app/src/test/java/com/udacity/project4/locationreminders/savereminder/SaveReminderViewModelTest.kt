package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeLocalRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is.isA
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects
    val TAG = javaClass.simpleName

    // This is for swaping out the main thread with a testing thread
    // so that determinism is gurantteed and test is not flaky
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var remindersLocalRepository: FakeLocalRepository

    // For testing LiveData
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        remindersLocalRepository = FakeLocalRepository()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersLocalRepository)
    }

    @Test
    fun saveReminderToRepo() {
        runBlockingTest {

            val title = "title"
            val description = "description"
            val location = "location"
            val latitude = 10.1
            val longitude = 10.1

            val reminder = ReminderDataItem(
                title = title,
                description = description,
                location = location,
                latitude = latitude,
                longitude = longitude,
                id = UUID.randomUUID().toString()
            )

            val id = reminder.id

            saveReminderViewModel.saveReminder(reminder)
            val result = saveReminderViewModel.dataSource.getReminder(id) as Result.Success<ReminderDTO>

            Assert.assertEquals(result.data.id, id)
            Assert.assertEquals(result.data.title, title)
            Assert.assertEquals(result.data.description, description)
            Assert.assertEquals(result.data.location, location)
            Assert.assertEquals(result.data.latitude, latitude)
            Assert.assertEquals(result.data.longitude, longitude)
        }
    }
}