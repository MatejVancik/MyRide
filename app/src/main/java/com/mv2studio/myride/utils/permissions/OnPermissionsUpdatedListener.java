/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils.permissions;

/**
 * Created by matej on 25/01/16.
 */
public interface OnPermissionsUpdatedListener {
    void onPermissions(@PermissionKind int permissionKind, boolean granted);
}