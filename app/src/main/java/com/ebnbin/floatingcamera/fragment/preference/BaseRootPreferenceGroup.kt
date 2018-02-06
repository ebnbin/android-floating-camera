package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.PreferenceScreen

/**
 * 基础偏好界面根偏好组.
 */
abstract class BaseRootPreferenceGroup(preferenceScreen: PreferenceScreen) : BasePreferenceGroup(preferenceScreen) {
    init {
        run { preferenceScreen.addPreference(this) }
    }
}
