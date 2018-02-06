package com.ebnbin.floatingcamera.fragment.preference

import com.ebnbin.floatingcamera.cameraHelper

/**
 * 摄像头偏好组.
 */
class DevicePreferenceGroup(rootPreferenceGroup: RootPreferenceGroup) : BasePreferenceGroup(rootPreferenceGroup) {
    init {
        rootPreferenceGroup.addPreferenceToGroup(this)
    }

    val isFrontPreference = IsFrontPreference(this)
    val backPreferenceGroup = if (cameraHelper.hasBackDevice) BackPreferenceGroup(this) else null
    val frontPreferenceGroup = if (cameraHelper.hasFrontDevice) FrontPreferenceGroup(this) else null
}
