package com.udacity.project4.locationreminders.reminderslist

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeLocalRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    val TAG = "RemindersListViewModelTest"

    // This is for swapping out the main thread with a testing thread
    // so that determinism is guaranteed and test is not flaky
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var remindersLocalRepository: FakeLocalRepository

    // For testing LiveData
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()

        remindersLocalRepository = FakeLocalRepository()

        val reminder1 = ReminderDTO(
            title = "title1",
            description = "description1",
            location = "location1",
            latitude = 10.1,
            longitude = 10.1
        )

        val reminder2 = ReminderDTO(
            title = "title2",
            description = "description2",
            location = "location2",
            latitude = 20.2,
            longitude = 20.2
        )

        val reminder3 = ReminderDTO(
            title = "title3",
            description = "description3",
            location = "location3",
            latitude = 30.3,
            longitude = 30.3
        )

        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

        remindersLocalRepository.addReminders(reminder1, reminder2, reminder3)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersLocalRepository)

    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadRemindersToReminderList() {
        runBlockingTest {
            remindersListViewModel.loadReminders()
            val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
            Assert.assertThat(remindersList[0], notNullValue())
            Assert.assertEquals(remindersList[0].title, remindersLocalRepository.reminders?.get(0)?.title)
            Assert.assertEquals(remindersList[1].description, remindersLocalRepository.reminders?.get(1)?.description)
            Assert.assertEquals(remindersList[2].location, remindersLocalRepository.reminders?.get(2)?.location)
            Assert.assertEquals(remindersList[0].latitude, remindersLocalRepository.reminders?.get(0)?.latitude)
            Assert.assertEquals(remindersList[1].longitude, remindersLocalRepository.reminders?.get(1)?.longitude)
        }
    }

    @Test
    fun loadReminders_showLoading() {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadRemindersWhenRemindersAreUnavailable_callErrorToDisplay() {
        remindersLocalRepository.setReturnError(true)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.error.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadRemindersWhenRemindersAreAvailable_callSuccessToDisplay() {
        remindersLocalRepository.setReturnError(false)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.error.getOrAwaitValue(), `is`(false))
    }

}