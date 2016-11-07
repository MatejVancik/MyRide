/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.mv2studio.myride.App;


/**
 * Created by matej on 06/06/15.
 */
public class Storage {

    public static boolean getBoolValue(String key) {
        SharedPreferences prefs = getDataPrefs();
        return prefs.getBoolean(key, false);
    }

    public static boolean getBoolValue(String key, boolean fallback) {
        SharedPreferences prefs = getDataPrefs();
        return prefs.getBoolean(key, fallback);
    }

    public static void storeBoolValue(String key, boolean value) {
        SharedPreferences prefs = getDataPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static int getIntValue(String key) {
        SharedPreferences prefs = getDataPrefs();
        return prefs.getInt(key, -1);
    }

    public static void storeIntValue(String key, int value) {
        SharedPreferences prefs = getDataPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    @Nullable
    public static String getString(String key) {
        SharedPreferences prefs = getDataPrefs();
        return prefs.getString(key, null);
    }

    public static void storeString(String key, String value) {
        SharedPreferences prefs = getDataPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static long getLongValue(String key) {
        SharedPreferences prefs = getDataPrefs();
        return prefs.getLong(key, -1);
    }

    public static void storeLongValue(String key, long value) {
        SharedPreferences prefs = getDataPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void deleteValueFromPrefs(String key) {
        SharedPreferences prefs = getDataPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void clearAll() {
        SharedPreferences prefs = getDataPrefs();
        prefs.edit().clear().apply();
    }

    private static SharedPreferences getDataPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }
}
