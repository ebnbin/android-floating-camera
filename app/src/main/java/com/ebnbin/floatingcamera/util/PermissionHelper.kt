package com.ebnbin.floatingcamera.util

import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * 权限帮助类.
 */
object PermissionHelper {
    fun isPermissionsGranted(vararg permissions: String) = permissions.none {
        ContextCompat.checkSelfPermission(app, it) != PackageManager.PERMISSION_GRANTED
    }

    fun isPermissionsDenied(vararg permissions: String) = !isPermissionsGranted(*permissions)
}
