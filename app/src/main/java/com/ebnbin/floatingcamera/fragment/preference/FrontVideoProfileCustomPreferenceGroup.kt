package com.ebnbin.floatingcamera.fragment.preference

import com.ebnbin.floatingcamera.util.extension.getValueIndex
import com.ebnbin.floatingcamera.util.extension.getValueSize

/**
 * 前置摄像头视频自定义配置偏好组.
 */
class FrontVideoProfileCustomPreferenceGroup(frontVideoProfilePreferenceGroup: FrontVideoProfilePreferenceGroup) :
        BasePreferenceGroup(frontVideoProfilePreferenceGroup) {
    init {
        frontVideoProfilePreferenceGroup.addPreferenceToGroup(this)
    }

    private val frontVideoResolutionPreference = FrontVideoResolutionPreference(this)

    init {
        isGroupVisible = frontVideoProfilePreferenceGroup.frontVideoProfilePreference.getValueIndex() ==
                frontVideoProfilePreferenceGroup.frontVideoProfilePreference.getValueSize() - 1
    }
}
