package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {
    val TAG = "Dev/" + javaClass.simpleName

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        intent?.let { it ->
            it.getStringExtra("woolahlah")?.let { it1 ->
                Log.d(TAG, it1) } }

        val reminder = intent?.let { it ->
            it.getSerializableExtra("EXTRA_ReminderDataItem") as ReminderDataItem }

        Log.d(TAG, "Reminder title: " + reminder?.title)
//        TODO: Add the implementation of the reminder details

        val titleContent = findViewById<TextView>(R.id.title_content)
        titleContent.text = reminder?.title

        val descriptionContent = findViewById<TextView>(R.id.description_content)
        descriptionContent.text = reminder?.description

        val location = findViewById<TextView>(R.id.location_content)
        location.text = reminder?.location

        val latitude = findViewById<TextView>(R.id.latitude_content)
        latitude.text = reminder?.latitude.toString()

        val longitude = findViewById<TextView>(R.id.longitude_content)
        longitude.text = reminder?.longitude.toString()
    }
}
