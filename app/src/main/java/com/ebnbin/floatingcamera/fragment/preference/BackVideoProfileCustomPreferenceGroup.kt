package com.ebnbin.floatingcamera.fragment.preference

import com.ebnbin.floatingcamera.util.extension.getValueIndex
import com.ebnbin.floatingcamera.util.extension.getValueSize

/**
 * 后置摄像头视频自定义配置偏好组.
 */
class BackVideoProfileCustomPreferenceGroup(backVideoProfilePreferenceGroup: BackVideoProfilePreferenceGroup) :
        BasePreferenceGroup(backVideoProfilePreferenceGroup) {
    init {
        backVideoProfilePreferenceGroup.addPreferenceToGroup(this)
    }

    private val backVideoResolutionPreference = BackVideoResolutionPreference(this)

    init {
        isGroupVisible = backVideoProfilePreferenceGroup.backVideoProfilePreference.getValueIndex() ==
                backVideoProfilePreferenceGroup.backVideoProfilePreference.getValueSize() - 1
    }
}
