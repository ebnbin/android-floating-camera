package com.ebnbin.floatingcamera.fragment.preference

/**
 * 前置摄像头偏好组.
 */
class FrontPreferenceGroup(devicePreferenceGroup: DevicePreferenceGroup) : BasePreferenceGroup(devicePreferenceGroup) {
    init {
        devicePreferenceGroup.addPreferenceToGroup(this)
    }

    val frontIsPhotoPreference = FrontIsPhotoPreference(this)
    val frontVideoPreferenceGroup = FrontVideoPreferenceGroup(this)
    val frontPhotoPreferenceGroup = FrontPhotoPreferenceGroup(this)

    init {
        isGroupVisible = devicePreferenceGroup.isFrontPreference.isChecked
    }
}
