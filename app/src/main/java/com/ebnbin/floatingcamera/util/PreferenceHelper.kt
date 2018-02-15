package com.ebnbin.floatingcamera.util

import android.content.SharedPreferences
import com.ebnbin.floatingcamera.fragment.preference.camera.CameraRootPreferenceGroup
import com.ebnbin.floatingcamera.fragment.preference.other.OtherRootPreferenceGroup
import com.ebnbin.floatingcamera.fragment.preference.window.WindowRootPreferenceGroup

/**
 * 偏好帮助类.
 */
object PreferenceHelper : SharedPreferences.OnSharedPreferenceChangeListener {
    init {
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        DebugHelper.log(key, "key")
    }

    /**
     * 摄像头.
     */
    fun device() = if (CameraRootPreferenceGroup.isFront)
        cameraHelper.frontDevice else
        cameraHelper.backDevice

    /**
     * 是否为照片 (or 视频).
     */
    fun isPhoto() = if (CameraRootPreferenceGroup.isFront)
        CameraRootPreferenceGroup.frontIsPhoto else
        CameraRootPreferenceGroup.backIsPhoto

    /**
     * 分辨率.
     */
    fun resolution() =
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
    fun videoProfile(): CameraHelper.Device.VideoProfile? {
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
     * 窗口大小.
     */
    fun windowSize(): WindowSize {
        val windowSizeValue = WindowRootPreferenceGroup.windowSize

        var landscapeWidth = displayRealSize.landscapeWidth * windowSizeValue / 100f
        var landscapeHeight = displayRealSize.landscapeHeight * windowSizeValue / 100f

        when (Preview.values()[WindowRootPreferenceGroup.preview.toInt()]) {
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
        }

        return WindowSize(landscapeWidth.toInt(), landscapeHeight.toInt(), true)
    }

    /**
     * 窗口位置.
     */
    fun windowPosition(): WindowPosition {
        val windowX = WindowRootPreferenceGroup.windowX
        val windowY = WindowRootPreferenceGroup.windowY
        val rotation = RotationHelper.getRotation()
        return WindowPosition(windowX, windowY, rotation)
    }

    /**
     * 是否为暗色主题 (or 亮色主题).
     */
    fun isDarkTheme() = OtherRootPreferenceGroup.isDarkTheme
}
