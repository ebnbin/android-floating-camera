package com.ebnbin.floatingcamera.fragment.preference

/**
 * 后置摄像头视频配置偏好组.
 */
class BackVideoProfilePreferenceGroup(backVideoPreferenceGroup: BackVideoPreferenceGroup) :
        BasePreferenceGroup(backVideoPreferenceGroup) {
    init {
        backVideoPreferenceGroup.addPreferenceToGroup(this)
    }

    val backVideoProfilePreference = BackVideoProfilePreference(this)
    val backVideoProfileCustomPreferenceGroup = BackVideoProfileCustomPreferenceGroup(this)
}
