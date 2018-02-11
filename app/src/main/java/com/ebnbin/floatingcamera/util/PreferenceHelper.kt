package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.fragment.preference.camera.CameraRootPreferenceGroup
import com.ebnbin.floatingcamera.fragment.preference.other.OtherRootPreferenceGroup

/**
 * 偏好帮助类.
 */
object PreferenceHelper {
    /**
     * 摄像头.
     */
    val device get() = if (CameraRootPreferenceGroup.isFront)
        cameraHelper.frontDevice else
        cameraHelper.backDevice

    /**
     * 是否为照片 (or 视频).
     */
    val isPhoto get() = if (CameraRootPreferenceGroup.isFront)
        CameraRootPreferenceGroup.frontIsPhoto else
        CameraRootPreferenceGroup.backIsPhoto

    /**
     * 分辨率.
     */
    val resolution: CameraHelper.Device.Resolution get() =
        if (CameraRootPreferenceGroup.isFront) {
            // 前置摄像头.
            val device = cameraHelper.frontDevice
            if (CameraRootPreferenceGroup.frontIsPhoto) {
                // 前置摄像头照片.
                device.photoResolutions[CameraRootPreferenceGroup.frontPhotoResolution.toInt()]
            } else {
                // 前置摄像头视频.
                val frontVideoProfileInt = CameraRootPreferenceGroup.frontVideoProfile.toInt()
                val videoProfiles = device.videoProfiles
                if (frontVideoProfileInt == videoProfiles.size/* - 1*/) {
                    // 前置摄像头视频自定义配置.
                    device.videoResolutions[CameraRootPreferenceGroup.frontVideoResolution.toInt()]
                } else if (frontVideoProfileInt in 0 until videoProfiles.size/* - 1*/) {
                    // 前置摄像头视频配置.
                    videoProfiles[frontVideoProfileInt].videoResolution
                } else {
                    throw BaseRuntimeException()
                }
            }
        } else {
            // 后置摄像头.
            val device = cameraHelper.backDevice
            if (CameraRootPreferenceGroup.backIsPhoto) {
                // 后置摄像头照片.
                device.photoResolutions[CameraRootPreferenceGroup.backPhotoResolution.toInt()]
            } else {
                // 后置摄像头视频.
                val backVideoProfileInt = CameraRootPreferenceGroup.backVideoProfile.toInt()
                val videoProfiles = device.videoProfiles
                if (backVideoProfileInt == videoProfiles.size/* - 1*/) {
                    // 后置摄像头视频自定义配置.
                    device.videoResolutions[CameraRootPreferenceGroup.backVideoResolution.toInt()]
                } else if (backVideoProfileInt in 0 until videoProfiles.size/* - 1*/) {
                    // 后置摄像头视频配置.
                    videoProfiles[backVideoProfileInt].videoResolution
                } else {
                    throw BaseRuntimeException()
                }
            }
        }

    /**
     * 视频配置.
     */
    val videoProfile: CameraHelper.Device.VideoProfile? get() {
        if (CameraRootPreferenceGroup.isFront && !CameraRootPreferenceGroup.frontIsPhoto) {
            val frontVideoProfileInt = CameraRootPreferenceGroup.frontVideoProfile.toInt()
            val videoProfiles = cameraHelper.frontDevice.videoProfiles
            if (frontVideoProfileInt in 0 until videoProfiles.size/* - 1*/) {
                return videoProfiles[frontVideoProfileInt]
            }
        } else if (!CameraRootPreferenceGroup.backIsPhoto) {
            val backVideoProfileInt = CameraRootPreferenceGroup.backVideoProfile.toInt()
            val videoProfiles = cameraHelper.backDevice.videoProfiles
            if (backVideoProfileInt in 0 until videoProfiles.size/* - 1*/) {
                return videoProfiles[backVideoProfileInt]
            }
        }

        return null
    }

    /**
     * 是否为暗色主题 (or 亮色主题).
     */
    val isDarkTheme get() = OtherRootPreferenceGroup.isDarkTheme
}
