package com.mv2studio.myride.extensions

import android.util.Log


/**
 * Created by matej on 16/11/2016.
 */

private val tag = "My Ride"
private val crashlyticsLogging = false

fun Any.d(message: String?) {
    Log.d(tag, message)
    //if (crashlyticsLogging) Crashlytics.log(message)
}

fun Any.w(message: String?) {
    Log.w(tag, message)
    //if (crashlyticsLogging) Crashlytics.log(message)
}

fun Any.e(message: String?) {
    Log.e(tag, message)
    //if (crashlyticsLogging) Crashlytics.log(message)
}

fun Any.i(message: String?) {
    Log.i(tag, message)
    //if (crashlyticsLogging) Crashlytics.log(message)
}

fun Any.d(message: String?, e: Throwable) {
    Log.d(tag, message, e)
    //if (crashlyticsLogging) Crashlytics.logException(e)
}

fun Any.w(message: String?, e: Throwable) {
    Log.w(tag, message, e)
    //if (crashlyticsLogging) Crashlytics.logException(e)
}

fun Any.e(message: String?, e: Throwable) {
    Log.e(tag, message, e)
    //if (crashlyticsLogging) Crashlytics.logException(e)
}

fun Any.i(message: String?, e: Throwable) {
    Log.i(tag, message, e)
    //if (crashlyticsLogging) Crashlytics.logException(e)
}