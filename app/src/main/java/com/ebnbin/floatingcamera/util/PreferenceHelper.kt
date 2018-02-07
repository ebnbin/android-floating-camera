package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.fragment.preference.IsDarkThemePreference
import com.ebnbin.floatingcamera.fragment.preference.IsFrontPreference

/**
 * 偏好帮助类.
 */
object PreferenceHelper {
    /**
     * 当前设备.
     */
    val device get() = if (IsFrontPreference.value) cameraHelper.frontDevice else cameraHelper.backDevice

    val isDarkTheme get() = IsDarkThemePreference.value
}
