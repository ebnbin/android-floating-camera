package com.ebnbin.floatingcamera.fragment.preference

/**
 * 前置摄像头照片偏好组.
 */
class FrontPhotoPreferenceGroup(frontPreferenceGroup: FrontPreferenceGroup) :
        BasePreferenceGroup(frontPreferenceGroup) {
    init {
        frontPreferenceGroup.addPreferenceToGroup(this)
    }

    private val frontPhotoResolutionPreference = FrontPhotoResolutionPreference(this)

    init {
        isGroupVisible = frontPreferenceGroup.frontIsPhotoPreference.isChecked
    }
}
