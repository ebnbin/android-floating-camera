package com.ebnbin.floatingcamera.fragment.preference

/**
 * 前置摄像头视频配置偏好组.
 */
class FrontVideoProfilePreferenceGroup(frontVideoPreferenceGroup: FrontVideoPreferenceGroup) :
        BasePreferenceGroup(frontVideoPreferenceGroup) {
    init {
        frontVideoPreferenceGroup.addPreferenceToGroup(this)
    }

    val frontVideoProfilePreference = FrontVideoProfilePreference(this)
    val frontVideoProfileCustomPreferenceGroup = FrontVideoProfileCustomPreferenceGroup(this)
}
