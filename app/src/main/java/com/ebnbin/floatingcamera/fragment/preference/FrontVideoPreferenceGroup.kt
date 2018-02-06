package com.ebnbin.floatingcamera.fragment.preference

/**
 * 前置摄像头视频偏好组.
 */
class FrontVideoPreferenceGroup(frontPreferenceGroup: FrontPreferenceGroup) :
        BasePreferenceGroup(frontPreferenceGroup) {
    init {
        frontPreferenceGroup.addPreferenceToGroup(this)
    }

    private val frontVideoResolutionPreference = FrontVideoResolutionPreference(this)

    init {
        isGroupVisible = !frontPreferenceGroup.frontIsPhotoPreference.isChecked
    }
}
