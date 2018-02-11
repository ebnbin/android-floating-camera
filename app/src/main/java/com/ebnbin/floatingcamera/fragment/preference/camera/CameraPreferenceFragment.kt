package com.ebnbin.floatingcamera.fragment.preference.camera

import android.support.v7.preference.PreferenceScreen
import com.ebnbin.floatingcamera.preference.PreferenceFragment

/**
 * 相机偏好界面.
 */
class CameraPreferenceFragment : PreferenceFragment() {
    override fun createRootPreferenceGroup(preferenceScreen: PreferenceScreen) =
            CameraRootPreferenceGroup(preferenceScreen.context)
}
