package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveReminders_retrievesReminders() = runBlocking {
        // GIVEN - a new reminder saved in the database

        val id = UUID.randomUUID().toString()

        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Taipei, Taiwan",
            latitude = 25.101624722772275,
            longitude = 121.54853129517073,
            id = id
        )

        remindersLocalRepository.saveReminder(reminder)

        // WHEN - reminder retrieved by id
        val loaded = remindersLocalRepository.getReminder(id)

        // THEN - same reminder is returned
        assert(loaded is Result.Success)
        loaded as Result.Success
        assertThat(loaded.data.title, `is`("Title"))
        assertThat(loaded.data.description, `is`("Description"))
        assertThat(loaded.data.location, `is`("Taipei, Taiwan"))
        assertThat(loaded.data.longitude, `is`(121.54853129517073))
        assertThat(loaded.data.latitude, `is`(25.101624722772275))
    }

    @Test
    fun noReminderInDb_getReminderByIdReturnsError() {
        // GIVEN - a random id not in database
        val id = UUID.randomUUID().toString()

        // WHEN - retrieve reminder of that id from database
        val loaded = runBlocking {
            remindersLocalRepository.getReminder(id)
        }

        // THEN - an error is returned
        assert(loaded is Result.Error)
    }

}