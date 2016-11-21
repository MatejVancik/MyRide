package com.mv2studio.myride.ui

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.mv2studio.myride.extensions.Android

/**
 * Created by matej on 18/11/2016.
 */
enum class PermissionKind(vararg val permissions: String) {
    CONTACTS(Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS),
    PHONE(Manifest.permission.CALL_PHONE),
    MEMORY(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE);
}

class PermissionsAskingActivity : AppCompatActivity() {

    private var permissionsResult: ((Boolean, PermissionKind) -> Unit)? = null

    fun requestPermissions(kind: PermissionKind, result: (granted: Boolean, kind: PermissionKind) -> Unit) {
        if (Android.isAPILevelBelow(23)) {
            result(true, kind)
            return
        }

        var hasPermissions = true

        for (permission in kind.permissions) {
            hasPermissions = hasPermissions and (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
        }

        if (hasPermissions) {
            result(hasPermissions, kind)
        } else {
            permissionsResult = result
            ActivityCompat.requestPermissions(this, kind.permissions, kind.ordinal)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        permissionsResult?.invoke(granted, PermissionKind.values()[requestCode])
        permissionsResult = null
    }


}