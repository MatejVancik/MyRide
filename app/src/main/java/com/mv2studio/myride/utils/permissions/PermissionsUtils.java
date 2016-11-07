/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.mv2studio.myride.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matej on 25/01/16.
 */
public class PermissionsUtils {

    private List<OnPermissionsUpdatedListener> mListeners;

    public static final String[] CONTACT_PERMISSIONS = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS};

    public static final String[] PHONE_PERMISSIONS = {
            Manifest.permission.CALL_PHONE
    };

    // not requierd below api 23 => will not crash on Ice cream sandwich
    public static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String[][] PERMISSIONS_ARRAY = {CONTACT_PERMISSIONS, PHONE_PERMISSIONS, STORAGE_PERMISSIONS};

    private static PermissionsUtils sInstance;

    private PermissionsUtils() {
        mListeners = new ArrayList<>();
    }

    public static PermissionsUtils getInstance() {
        if (sInstance == null) sInstance = new PermissionsUtils();
        return sInstance;
    }

    public void onPermissionsUpdated(boolean granted, @PermissionKind int permissionKind) {
        for (OnPermissionsUpdatedListener listener : mListeners) {
            if (listener != null) listener.onPermissions(permissionKind, granted);
        }
    }

    public boolean hasPermissions(Activity activity, @PermissionKind int permissionKind) {
        if (Utils.isApiBelow(23)) return true;

        boolean hasPermission = true;

        for (String permission : PERMISSIONS_ARRAY[permissionKind]) {
            hasPermission &= ContextCompat.checkSelfPermission(activity, permission)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return hasPermission;
    }

    public boolean requestPermission(Activity activity, @PermissionKind int permissionKind,
                                     OnPermissionsUpdatedListener listener) {
        if (Utils.isApiBelow(23)) return true;
        boolean hasPermission = hasPermissions(activity, permissionKind);

        if (hasPermission) return true;
        else {
            mListeners.add(listener);
            ActivityCompat.requestPermissions(activity, PERMISSIONS_ARRAY[permissionKind], permissionKind);
        }

        return false;
    }

}
