/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils.permissions;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by matej on 25/01/16.
 */
@Retention(SOURCE)
@IntDef({PermissionKind.PERMISSIONS_REQUEST_CONTACTS, PermissionKind.PERMISSIONS_REQUEST_PHONE,
        PermissionKind.PERMISSIONS_REQUEST_MEMORY})
public @interface PermissionKind {
    int PERMISSIONS_REQUEST_CONTACTS = 0;
    int PERMISSIONS_REQUEST_PHONE = 1;
    int PERMISSIONS_REQUEST_MEMORY = 2;
}