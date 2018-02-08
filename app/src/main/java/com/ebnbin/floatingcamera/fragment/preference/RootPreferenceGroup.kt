package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.PreferenceScreen

/**
 * 偏好界面根偏好组.
 */
class RootPreferenceGroup(preferenceScreen: PreferenceScreen) : BaseRootPreferenceGroup(preferenceScreen) {
    private val devicePreferenceGroup = DevicePreferenceGroup(this)
    private val previewPreference = PreviewPreference(this)
    private val windowSizePreference = WindowSizePreference(this)
    private val windowXPreference = WindowXPreference(this)
    private val windowYPreference = WindowYPreference(this)
    private val isDarkThemePreference = IsDarkThemePreference(this)
}
