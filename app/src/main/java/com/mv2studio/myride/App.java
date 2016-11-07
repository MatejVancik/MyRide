package com.mv2studio.myride;

import android.app.Application;
import android.content.Context;

import com.mv2studio.myride.connection.ConnectionManager;

/**
 * Created by matej on 29.10.16.
 */

public class App extends Application {

    private static App sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        ConnectionManager.init();

    }

    public static Context getAppContext() {
        return sContext;
    }
}
