package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull.nullValue
import org.junit.After
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    val TAG = "RemindersDaoTest"

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertAndRetrieveReminderDto() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Taipei, Taiwan",
            latitude = 25.101624722772275,
            longitude = 121.54853129517073
        )

        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected value
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue()) // check data isn't null
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminders_returnsAListOfReminders() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Taipei, Taiwan",
            latitude = 25.101624722772275,
            longitude = 121.54853129517073
        )

        database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected value
        assertThat<List<ReminderDTO>>(loaded as List<ReminderDTO>, notNullValue()) // check data isn't null
        assertThat(loaded[0].id, `is`(reminder.id))
        assertThat(loaded[0].title, `is`(reminder.title))
        assertThat(loaded[0].description, `is`(reminder.description))
        assertThat(loaded[0].location, `is`(reminder.location))
        assertThat(loaded[0].latitude, `is`(reminder.latitude))
        assertThat(loaded[0].longitude, `is`(reminder.longitude))

    }

    @Test
    fun getRemindersAfterDeleteAll_returnsNull() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Taipei, Taiwan",
            latitude = 25.101624722772275,
            longitude = 121.54853129517073
        )

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()

        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected value
        assert(loaded.isNullOrEmpty())
    }

}