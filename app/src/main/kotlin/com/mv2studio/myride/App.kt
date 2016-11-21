package com.mv2studio.myride

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.mv2studio.myride.connection.ConnectionManager

/**
 * Created by matej on 29.10.16.
 */

class App : Application() {

    companion object {
        var appContext: App = null!!
            private set

        var preferences: SharedPreferences = null!!
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
    }


}
