package com.ebnbin.floatingcamera.fragment.preference.other

import android.support.v7.preference.PreferenceScreen
import com.ebnbin.floatingcamera.preference.PreferenceFragment

/**
 * 其他偏好界面.
 */
class OtherPreferenceFragment : PreferenceFragment() {
    override fun createRootPreferenceGroup(preferenceScreen: PreferenceScreen) =
            OtherRootPreferenceGroup(preferenceScreen.context)
}
