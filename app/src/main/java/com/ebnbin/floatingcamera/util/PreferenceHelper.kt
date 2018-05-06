package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.fragment.more.MorePreferenceFragment
import com.ebnbin.floatingcamera.fragment.preference.CameraPreferenceFragment
import com.ebnbin.floatingcamera.fragment.preference.WindowPreferenceFragment

/**
 * 偏好帮助类.
 */
object PreferenceHelper {
    /**
     * 摄像头.
     */
    fun device() = if (CameraPreferenceFragment.isFront)
        CameraHelper.frontDevice else
        CameraHelper.backDevice

    /**
     * 是否为照片 (or 视频).
     */
    fun isPhoto() = if (CameraPreferenceFragment.isFront)
        CameraPreferenceFragment.frontIsPhoto else
        CameraPreferenceFragment.backIsPhoto

    /**
     * 分辨率.
     */
    fun resolution() =
        if (CameraPreferenceFragment.isFront) {
            // 前置摄像头.
            val device = CameraHelper.frontDevice
            if (CameraPreferenceFragment.frontIsPhoto) {
                // 前置摄像头照片.
                device.photoResolutions[CameraPreferenceFragment.frontPhotoResolution.toInt()]
            } else {
                // 前置摄像头视频.
                val frontVideoProfileInt = CameraPreferenceFragment.frontVideoProfile.toInt()
                val videoProfiles = device.videoProfiles
                if (frontVideoProfileInt in 0 until videoProfiles.size) {
                    // 前置摄像头视频配置.
                    videoProfiles[frontVideoProfileInt].videoResolution
                } else throw BaseRuntimeException()
            }
        } else {
            // 后置摄像头.
            val device = CameraHelper.backDevice
            if (CameraPreferenceFragment.backIsPhoto) {
                // 后置摄像头照片.
                device.photoResolutions[CameraPreferenceFragment.backPhotoResolution.toInt()]
            } else {
                // 后置摄像头视频.
                val backVideoProfileInt = CameraPreferenceFragment.backVideoProfile.toInt()
                val videoProfiles = device.videoProfiles
                if (backVideoProfileInt in 0 until videoProfiles.size) {
                    // 后置摄像头视频配置.
                    videoProfiles[backVideoProfileInt].videoResolution
                } else throw BaseRuntimeException()
            }
        }

    /**
     * 视频配置.
     */
    fun videoProfile(): CameraHelper.Device.VideoProfile {
        return if (CameraPreferenceFragment.isFront && !CameraPreferenceFragment.frontIsPhoto) {
            val frontVideoProfileInt = CameraPreferenceFragment.frontVideoProfile.toInt()
            val videoProfiles = CameraHelper.frontDevice.videoProfiles
            if (frontVideoProfileInt in 0 until videoProfiles.size) {
                videoProfiles[frontVideoProfileInt]
            } else throw BaseRuntimeException()
        } else if (!CameraPreferenceFragment.backIsPhoto) {
            val backVideoProfileInt = CameraPreferenceFragment.backVideoProfile.toInt()
            val videoProfiles = CameraHelper.backDevice.videoProfiles
            if (backVideoProfileInt in 0 until videoProfiles.size) {
                videoProfiles[backVideoProfileInt]
            } else throw BaseRuntimeException()
        } else throw BaseRuntimeException()
    }

    /**
     * 窗口大小.
     */
    fun windowSize(): Size {
        val windowSizeValue = WindowPreferenceFragment.windowSize

        var landscapeWidth = displayRealSize.width(true) * windowSizeValue / 100f
        var landscapeHeight = displayRealSize.height(true) * windowSizeValue / 100f

        when (Preview.values()[CameraPreferenceFragment.preview.toInt()]) {
            Preview.CAPTURE -> {
                val resolution = resolution()
                if (landscapeWidth < landscapeHeight * resolution.landscapeWidth / resolution.landscapeHeight) {
                    landscapeHeight = landscapeWidth * resolution.landscapeHeight / resolution.landscapeWidth
                } else {
                    landscapeWidth = landscapeHeight * resolution.landscapeWidth / resolution.landscapeHeight
                }
            }
            Preview.FULL -> {
                val previewResolution = device().previewResolution
                if (landscapeWidth < landscapeHeight * previewResolution.landscapeWidth /
                        previewResolution.landscapeHeight) {
                    landscapeHeight = landscapeWidth * previewResolution.landscapeHeight /
                            previewResolution.landscapeWidth
                } else {
                    landscapeWidth = landscapeHeight * previewResolution.landscapeWidth /
                            previewResolution.landscapeHeight
                }
            }
            Preview.SCREEN -> {
                // Do nothing.
            }
            Preview.SQUARE -> {
                landscapeWidth = landscapeHeight
            }
        }

        return Size(landscapeWidth.toInt(), landscapeHeight.toInt(), true)
    }

    /**
     * 窗口位置.
     */
    fun windowPosition(): Position {
        val windowX = WindowPreferenceFragment.windowX
        val windowY = WindowPreferenceFragment.windowY
        val rotation = RotationHelper.getRotation()
        return Position(windowX, windowY, rotation)
    }

    /**
     * 是否为暗色主题 (or 亮色主题).
     */
    fun isDarkTheme() = MorePreferenceFragment.isDarkTheme
}
