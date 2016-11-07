/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils;

/**
 * Created by matej on 06/06/15.
 */
public class Log {

    private static final String TAG = "MY_RIDE";
    private static final boolean CRASHLYTICS_LOGING = false;

    public static void d(String message) {
        android.util.Log.d(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
        }
    }
    public static void w(String message) {
        android.util.Log.w(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
        }
    }
    public static void e(String message) {
        android.util.Log.e(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
        }
    }
    public static void i(String message) {
        android.util.Log.i(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
        }
    }

    public static void d(String message, Throwable e) {
        android.util.Log.d(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
//            Crashlytics.logException(e);
        }
    }
    public static void w(String message, Throwable e) {
        android.util.Log.w(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
//            Crashlytics.logException(e);
        }
    }
    public static void e(String message, Throwable e) {
        android.util.Log.e(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
//            Crashlytics.logException(e);
        }
    }
    public static void i(String message, Throwable e) {
        android.util.Log.i(TAG, message);
        if (CRASHLYTICS_LOGING) {
//            Crashlytics.log(message);
//            Crashlytics.logException(e);
        }
    }
}
