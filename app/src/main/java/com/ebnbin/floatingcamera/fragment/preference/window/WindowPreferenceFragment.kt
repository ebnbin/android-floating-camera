package com.ebnbin.floatingcamera.fragment.preference.window

import android.support.v7.preference.PreferenceScreen
import com.ebnbin.floatingcamera.preference.PreferenceFragment

/**
 * 窗口偏好界面.
 */
class WindowPreferenceFragment : PreferenceFragment() {
    override fun createRootPreferenceGroup(preferenceScreen: PreferenceScreen) =
            WindowRootPreferenceGroup(preferenceScreen.context)
}
