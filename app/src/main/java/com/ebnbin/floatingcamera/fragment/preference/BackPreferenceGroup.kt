package com.ebnbin.floatingcamera.fragment.preference

/**
 * 后置摄像头偏好组.
 */
class BackPreferenceGroup(devicePreferenceGroup: DevicePreferenceGroup) : BasePreferenceGroup(devicePreferenceGroup) {
    init {
        devicePreferenceGroup.addPreferenceToGroup(this)
    }

    val backIsPhotoPreference = BackIsPhotoPreference(this)
    val backVideoPreferenceGroup = BackVideoPreferenceGroup(this)
    val backPhotoPreferenceGroup = BackPhotoPreferenceGroup(this)

    init {
        isGroupVisible = !devicePreferenceGroup.isFrontPreference.isChecked
    }
}
