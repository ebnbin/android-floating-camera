package com.ebnbin.floatingcamera.fragment.preference

/**
 * 后置摄像头视频偏好组.
 */
class BackVideoPreferenceGroup(backPreferenceGroup: BackPreferenceGroup) : BasePreferenceGroup(backPreferenceGroup) {
    init {
        backPreferenceGroup.addPreferenceToGroup(this)
    }

    private val backVideoResolutionPreference = BackVideoResolutionPreference(this)

    init {
        isGroupVisible = !backPreferenceGroup.backIsPhotoPreference.isChecked
    }
}
