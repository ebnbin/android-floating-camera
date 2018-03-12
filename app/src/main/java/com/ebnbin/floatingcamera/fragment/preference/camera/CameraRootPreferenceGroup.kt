package com.ebnbin.floatingcamera.fragment.preference.camera

import android.content.Context
import android.content.SharedPreferences
import android.support.v14.preference.SwitchPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.FooterPreference
import com.ebnbin.floatingcamera.preference.ListPreference
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.getValueIndex
import com.ebnbin.floatingcamera.util.extension.getValueSize
import com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues

/**
 * 相机根偏好组.
 *
 *     CameraPreferenceGroup
 *         DevicePreferenceGroup
 *             IsFrontPreference
 *             BackPreferenceGroup?
 *                 BackIsPhotoPreference
 *                 BackVideoPreferenceGroup
 *                     BackVideoProfilePreferenceGroup
 *                         BackVideoProfilePreference
 *                         BackVideoProfileCustomPreferenceGroup
 *                             BackVideoResolutionPreference
 *                 BackPhotoPreferenceGroup
 *                     BackPhotoResolutionPreference
 *             FrontPreferenceGroup?
 *                 FrontIsPhotoPreference
 *                 FrontVideoPreferenceGroup
 *                     FrontVideoProfilePreferenceGroup
 *                         FrontVideoProfilePreference
 *                         FrontVideoProfileCustomPreferenceGroup
 *                             FrontVideoResolutionPreference
 *                 FrontPhotoPreferenceGroup
 *                     FrontPhotoResolutionPreference
 *     FooterPreference
 */
class CameraRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
    /**
     * 相机偏好组.
     */
    private val cameraPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        devicePreferenceGroup))
    }

    /**
     * 摄像头组.
     */
    private val devicePreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        isFrontPreference,
                        backPreferenceGroup,
                        frontPreferenceGroup))
    }

    /**
     * 后置/前置摄像头.
     */
    private val isFrontPreference by lazy {
        SwitchPreference(context).apply {
            key = KEY_IS_FRONT
            setDefaultValue(DEF_VALUE_IS_FRONT)
            isEnabled = cameraHelper.hasBothDevices
            setTitle(R.string.is_front_title)
            setSummaryOff(R.string.is_front_summary_off)
            setSummaryOn(R.string.is_front_summary_on)
        }
    }

    /**
     * 后置摄像头组.
     */
    private val backPreferenceGroup by lazy {
        if (!cameraHelper.hasBackDevice) return@lazy null

        PreferenceGroup(context,
                preferences = arrayOf(
                        backIsPhotoPreference,
                        backVideoPreferenceGroup,
                        backPhotoPreferenceGroup),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = !isFrontPreference.isChecked
                })
    }

    /**
     * 后置摄像头视频/照片.
     */
    private val backIsPhotoPreference by lazy {
        SwitchPreference(context).apply {
            key = KEY_BACK_IS_PHOTO
            setDefaultValue(DEF_VALUE_BACK_IS_PHOTO)
            setTitle(R.string.is_photo_title)
            setSummaryOff(R.string.back_is_photo_summary_off)
            setSummaryOn(R.string.back_is_photo_summary_on)
        }
    }

    /**
     * 后置摄像头视频组.
     */
    private val backVideoPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        backVideoProfilePreferenceGroup),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = !backIsPhotoPreference.isChecked
                })
    }

    /**
     * 后置摄像头视频配置组.
     */
    private val backVideoProfilePreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        backVideoProfilePreference,
                        backVideoProfileCustomPreferenceGroup))
    }

    /**
     * 后置摄像头视频配置.
     */
    private val backVideoProfilePreference by lazy {
        ListPreference(context).apply {
            key = KEY_BACK_VIDEO_PROFILE
            setDefaultValue(DEF_VALUE_BACK_VIDEO_PROFILE)
            setTitle(R.string.back_video_profile_title)
            setEntriesAndEntryValues(cameraHelper.backDevice.videoProfileSummaries)
            summaries = cameraHelper.backDevice.videoProfileSummaries
            setDialogTitle(R.string.back_video_profile_title)
        }
    }

    /**
     * 后置摄像头视频自定义配置组.
     */
    private val backVideoProfileCustomPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        backVideoResolutionPreference),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = backVideoProfilePreference.getValueIndex() ==
                            backVideoProfilePreference.getValueSize() - 1
                })
    }

    /**
     * 后置摄像头视频分辨率.
     */
    private val backVideoResolutionPreference by lazy {
        ListPreference(context).apply {
            key = KEY_BACK_VIDEO_RESOLUTION
            setDefaultValue(DEF_VALUE_BACK_VIDEO_RESOLUTION)
            setTitle(R.string.back_video_resolution_title)
            setEntriesAndEntryValues(cameraHelper.backDevice.videoResolutionSummaries)
            summaries = cameraHelper.backDevice.videoResolutionSummaries
            setDialogTitle(R.string.back_video_resolution_title)
        }
    }

    /**
     * 后置摄像头照片组.
     */
    private val backPhotoPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        backPhotoResolutionPreference),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = backIsPhotoPreference.isChecked
                })
    }

    /**
     * 后置摄像头照片分辨率.
     */
    private val backPhotoResolutionPreference by lazy {
        ListPreference(context).apply {
            key = KEY_BACK_PHOTO_RESOLUTION
            setDefaultValue(DEF_VALUE_BACK_PHOTO_RESOLUTION)
            setTitle(R.string.back_photo_resolution_title)
            setEntriesAndEntryValues(cameraHelper.backDevice.photoResolutionSummaries)
            summaries = cameraHelper.backDevice.photoResolutionSummaries
            setDialogTitle(R.string.back_photo_resolution_title)
        }
    }

    /**
     * 前置摄像头组.
     */
    private val frontPreferenceGroup by lazy {
        if (!cameraHelper.hasFrontDevice) return@lazy null

        PreferenceGroup(context,
                preferences = arrayOf(
                        frontIsPhotoPreference,
                        frontVideoPreferenceGroup,
                        frontPhotoPreferenceGroup),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = isFrontPreference.isChecked
                })
    }

    /**
     * 前置摄像头视频/照片.
     */
    private val frontIsPhotoPreference by lazy {
        SwitchPreference(context).apply {
            key = KEY_FRONT_IS_PHOTO
            setDefaultValue(DEF_VALUE_FRONT_IS_PHOTO)
            setTitle(R.string.is_photo_title)
            setSummaryOff(R.string.front_is_photo_summary_off)
            setSummaryOn(R.string.front_is_photo_summary_on)
        }
    }

    /**
     * 前置摄像头视频组.
     */
    private val frontVideoPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        frontVideoProfilePreferenceGroup),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = !frontIsPhotoPreference.isChecked
                })
    }

    /**
     * 前置摄像头视频配置组.
     */
    private val frontVideoProfilePreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        frontVideoProfilePreference,
                        frontVideoProfileCustomPreferenceGroup))
    }

    /**
     * 前置摄像头视频配置.
     */
    private val frontVideoProfilePreference by lazy {
        ListPreference(context).apply {
            key = KEY_FRONT_VIDEO_PROFILE
            setDefaultValue(DEF_VALUE_FRONT_VIDEO_PROFILE)
            setTitle(R.string.front_video_profile_title)
            setEntriesAndEntryValues(cameraHelper.frontDevice.videoProfileSummaries)
            summaries = cameraHelper.frontDevice.videoProfileSummaries
            setDialogTitle(R.string.front_video_profile_title)
        }
    }

    /**
     * 前置摄像头视频自定义配置组.
     */
    private val frontVideoProfileCustomPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        frontVideoResolutionPreference),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = frontVideoProfilePreference.getValueIndex() ==
                            frontVideoProfilePreference.getValueSize() - 1
                })
    }

    /**
     * 前置摄像头视频分辨率.
     */
    private val frontVideoResolutionPreference by lazy {
        ListPreference(context).apply {
            key = KEY_FRONT_VIDEO_RESOLUTION
            setDefaultValue(DEF_VALUE_FRONT_VIDEO_RESOLUTION)
            setTitle(R.string.front_video_resolution_title)
            setEntriesAndEntryValues(cameraHelper.frontDevice.videoResolutionSummaries)
            summaries = cameraHelper.frontDevice.videoResolutionSummaries
            setDialogTitle(R.string.front_video_resolution_title)
        }
    }

    /**
     * 前置摄像头照片组.
     */
    private val frontPhotoPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        frontPhotoResolutionPreference),
                onFirstAttachedToHierarchy = {
                    it.isGroupVisible = frontIsPhotoPreference.isChecked
                })
    }

    /**
     * 前置摄像头照片分辨率.
     */
    private val frontPhotoResolutionPreference by lazy {
        ListPreference(context).apply {
            key = KEY_FRONT_PHOTO_RESOLUTION
            setDefaultValue(DEF_VALUE_FRONT_PHOTO_RESOLUTION)
            setTitle(R.string.front_photo_resolution_title)
            setEntriesAndEntryValues(cameraHelper.frontDevice.photoResolutionSummaries)
            summaries = cameraHelper.frontDevice.photoResolutionSummaries
            setDialogTitle(R.string.front_photo_resolution_title)
        }
    }

    /**
     * 底部偏好.
     */
    private val footerPreference by lazy {
        FooterPreference(context)
    }

    override fun preferences() = arrayOf(
            cameraPreferenceGroup,
            footerPreference)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_IS_FRONT -> {
                val newValue = isFront

                backPreferenceGroup?.isGroupVisible = !newValue
                frontPreferenceGroup?.isGroupVisible = newValue
            }
            KEY_BACK_VIDEO_PROFILE -> {
                val preference = backVideoProfilePreference
                val newValue = backVideoProfile

                backVideoProfileCustomPreferenceGroup.isGroupVisible =
                        newValue == preference.entryValues[preference.getValueSize() - 1].toString()
            }
            KEY_BACK_IS_PHOTO -> {
                val newValue = backIsPhoto

                backVideoPreferenceGroup.isGroupVisible = !newValue
                backPhotoPreferenceGroup.isGroupVisible = newValue
            }
            KEY_FRONT_VIDEO_PROFILE -> {
                val preference = frontVideoProfilePreference
                val newValue = frontVideoProfile

                frontVideoProfileCustomPreferenceGroup.isGroupVisible =
                        newValue == preference.entryValues[preference.getValueSize() - 1].toString()
            }
            KEY_FRONT_IS_PHOTO -> {
                val newValue = frontIsPhoto

                frontVideoPreferenceGroup.isGroupVisible = !newValue
                frontPhotoPreferenceGroup.isGroupVisible = newValue
            }
        }
    }

    companion object {
        private const val KEY_IS_FRONT = "is_front"
        private const val KEY_BACK_IS_PHOTO = "back_is_photo"
        private const val KEY_BACK_VIDEO_PROFILE = "back_video_profile"
        private const val KEY_BACK_VIDEO_RESOLUTION = "back_video_resolution"
        private const val KEY_BACK_PHOTO_RESOLUTION = "back_photo_resolution"
        private const val KEY_FRONT_IS_PHOTO = "front_is_photo"
        private const val KEY_FRONT_VIDEO_PROFILE = "front_video_profile"
        private const val KEY_FRONT_VIDEO_RESOLUTION = "front_video_resolution"
        private const val KEY_FRONT_PHOTO_RESOLUTION = "front_photo_resolution"

        private val DEF_VALUE_IS_FRONT get() = !cameraHelper.hasBothDevices && cameraHelper.hasFrontDevice
        private const val DEF_VALUE_BACK_IS_PHOTO = false
        private const val DEF_VALUE_BACK_VIDEO_PROFILE = "0"
        private const val DEF_VALUE_BACK_VIDEO_RESOLUTION = "0"
        private const val DEF_VALUE_BACK_PHOTO_RESOLUTION = "0"
        private const val DEF_VALUE_FRONT_IS_PHOTO = false
        private const val DEF_VALUE_FRONT_VIDEO_PROFILE = "0"
        private const val DEF_VALUE_FRONT_VIDEO_RESOLUTION = "0"
        private const val DEF_VALUE_FRONT_PHOTO_RESOLUTION = "0"

        val isFront get() = defaultSharedPreferences.get(KEY_IS_FRONT, DEF_VALUE_IS_FRONT)
        val backIsPhoto get() = defaultSharedPreferences.get(KEY_BACK_IS_PHOTO, DEF_VALUE_BACK_IS_PHOTO)
        val backVideoProfile get() = defaultSharedPreferences.get(KEY_BACK_VIDEO_PROFILE, DEF_VALUE_BACK_VIDEO_PROFILE)
        val backVideoResolution get() = defaultSharedPreferences.get(KEY_BACK_VIDEO_RESOLUTION,
                DEF_VALUE_BACK_VIDEO_RESOLUTION)
        val backPhotoResolution get() = defaultSharedPreferences.get(KEY_BACK_PHOTO_RESOLUTION,
                DEF_VALUE_BACK_PHOTO_RESOLUTION)
        val frontIsPhoto get() = defaultSharedPreferences.get(KEY_FRONT_IS_PHOTO, DEF_VALUE_FRONT_IS_PHOTO)
        val frontVideoProfile get() = defaultSharedPreferences.get(KEY_FRONT_VIDEO_PROFILE,
                DEF_VALUE_FRONT_VIDEO_PROFILE)
        val frontVideoResolution get() = defaultSharedPreferences.get(KEY_FRONT_VIDEO_RESOLUTION,
                DEF_VALUE_FRONT_VIDEO_RESOLUTION)
        val frontPhotoResolution get() = defaultSharedPreferences.get(KEY_FRONT_PHOTO_RESOLUTION,
                DEF_VALUE_FRONT_PHOTO_RESOLUTION)
    }
}
