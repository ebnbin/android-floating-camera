package com.ebnbin.floatingcamera.fragment.preference

/**
 * 后置摄像头照片偏好组.
 */
class BackPhotoPreferenceGroup(backPreferenceGroup: BackPreferenceGroup) : BasePreferenceGroup(backPreferenceGroup) {
    init {
        backPreferenceGroup.addPreferenceToGroup(this)
    }

    private val backPhotoResolutionPreference = BackPhotoResolutionPreference(this)

    init {
        isGroupVisible = backPreferenceGroup.backIsPhotoPreference.isChecked
    }
}
