package com.udacity.project4.locationreminders.reminderslist

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.JVM)
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class Junit4RunOrderTest {
    val TAG = javaClass.simpleName

    @Before
    fun init() {
        Log.d(TAG, "1")
    }

    @After
    fun tearDown() {
        Log.d(TAG, "6")
    }

    @Test
    fun firstTest() {
        Log.d(TAG, "2")
    }

    @Test
    fun secondTest() {
        Log.d(TAG, "3")
    }

    @Test
    fun thirdTest() {
        Log.d(TAG, "4")
    }

    @Test
    fun fourthTest() {
        Log.d(TAG, "5")
    }
}