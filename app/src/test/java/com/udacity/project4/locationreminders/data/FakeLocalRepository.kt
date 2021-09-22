package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeLocalRepository(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    //    TODO: Create a fake data source to act as a double to the real data source
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (!shouldReturnError) {
            reminders?.let {
                return Result.Success(ArrayList(it))
            }
        }

        return Result.Error(message = "Sorry, no data. You need to look somewhere else man.",
            statusCode = null)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!shouldReturnError) {
            reminders?.let {
                return Result.Success(ArrayList(it)[0])
            }
        }

        return Result.Error(
            message = "Sorry, no data. You need to look somewhere else man.",
            statusCode = null
        )
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun addReminders(vararg remindersArgs: ReminderDTO) {
        for (reminder in remindersArgs) {
            reminders?.add(reminder)
        }
    }
}