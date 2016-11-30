package com.mv2studio.myride.extensions

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.mv2studio.myride.App

/**
 * Created by matej on 16/11/2016.
 */

val context = App.appContext
val assets = context.assets
val res = context.resources

fun Int.asDpToPx(): Int = ((this * res.displayMetrics.density) + .5f).toInt()

fun Int.asPxToDp(): Int = (((this / res.displayMetrics.density) + .5f).toInt())

fun Activity.startLocationActivity(address: String) {
    val gmmInentUri = Uri.parse("geo:0,0?q=$address")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmInentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)
}

fun Activity.hideKeyboard() {
    if (currentFocus == null) return
    currentFocus.clearFocus()
    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(currentFocus.windowToken, 0)
}

fun Activity.hideKeyboardOnTouchOutside(view: View) {
    if (view !is EditText) {
        view.setOnTouchListener { view, motionEvent -> hideKeyboard(); false }
    }

    if (view is ViewGroup) {
        for (i in 0..view.childCount) {
            hideKeyboardOnTouchOutside(view.getChildAt(i))
        }
    }
}

/**
 * Use put* methods inside block to save specific values.
 */
inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    editor.func()
    editor.apply()
}

class Android {
    companion object {
        fun isAPILevelAtLeast(level: Int) = SDK_INT >= level

        fun isAPILevelBelow(level: Int) = SDK_INT < level
    }
}
