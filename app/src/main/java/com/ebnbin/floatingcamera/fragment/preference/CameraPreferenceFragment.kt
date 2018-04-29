package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.ListPreference
import com.ebnbin.floatingcamera.preference.PreferenceFragment
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.util.CameraHelper
import com.ebnbin.floatingcamera.util.Preview
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues
import com.ebnbin.floatingcamera.util.sp

/**
 * 相机偏好界面.
 */
class CameraPreferenceFragment : PreferenceFragment<CameraPreferenceFragment.CameraRootPreferenceGroup>() {
    override fun createRootPreferenceGroup(context: Context) = CameraRootPreferenceGroup(context)

    /**
     *     DevicePreferenceGroup
     *         IsFrontPreference
     *         BackPreferenceGroup?
     *             BackIsPhotoPreference
     *             BackVideoPreferenceGroup
     *                 BackVideoProfilePreferenceGroup
     *                     BackVideoProfilePreference
     *             BackPhotoPreferenceGroup
     *                 BackPhotoResolutionPreference
     *         FrontPreferenceGroup?
     *             FrontIsPhotoPreference
     *             FrontVideoPreferenceGroup
     *                 FrontVideoProfilePreferenceGroup
     *                     FrontVideoProfilePreference
     *             FrontPhotoPreferenceGroup
     *                 FrontPhotoResolutionPreference
     */
    class CameraRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
        /**
         * 摄像头组.
         */
        private val devicePreferenceGroup by lazy {
            PreferenceGroup(context).apply {
                initPreferences(
                        isFrontPreference,
                        backPreferenceGroup,
                        frontPreferenceGroup)
            }
        }

        /**
         * 后置/前置摄像头.
         */
        private val isFrontPreference by lazy {
            SwitchPreference(context).apply {
                key = KEY_IS_FRONT
                setDefaultValue(DEF_VALUE_IS_FRONT)
                isEnabled = CameraHelper.hasBothDevices
                setTitle(R.string.is_front_title)
                setSummaryOff(R.string.is_front_summary_off)
                setSummaryOn(R.string.is_front_summary_on)
            }
        }

        /**
         * 后置摄像头组.
         */
        private val backPreferenceGroup by lazy {
            if (!CameraHelper.hasBackDevice) return@lazy null

            PreferenceGroup(context).apply {
                initPreferences(
                        backIsPhotoPreference,
                        backVideoPreferenceGroup,
                        backPhotoPreferenceGroup)
                initIsGroupVisible { !isFrontPreference.isChecked }
            }
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
            PreferenceGroup(context).apply {
                initPreferences(
                        backVideoProfilePreferenceGroup)
                initIsGroupVisible { !backIsPhotoPreference.isChecked }
            }
        }

        /**
         * 后置摄像头视频配置组.
         */
        private val backVideoProfilePreferenceGroup by lazy {
            PreferenceGroup(context).apply {
                initPreferences(
                        backVideoProfilePreference)
            }
        }

        /**
         * 后置摄像头视频配置.
         */
        private val backVideoProfilePreference by lazy {
            ListPreference(context).apply {
                key = KEY_BACK_VIDEO_PROFILE
                setDefaultValue(DEF_VALUE_BACK_VIDEO_PROFILE)
                setTitle(R.string.back_video_profile_title)
                setEntriesAndEntryValues(CameraHelper.backDevice.videoProfileSummaries)
                summaries = CameraHelper.backDevice.videoProfileSummaries
                setDialogTitle(R.string.back_video_profile_title)
            }
        }

        /**
         * 后置摄像头照片组.
         */
        private val backPhotoPreferenceGroup by lazy {
            PreferenceGroup(context).apply {
                initPreferences(
                        backPhotoResolutionPreference)
                initIsGroupVisible { backIsPhotoPreference.isChecked }
            }
        }

        /**
         * 后置摄像头照片分辨率.
         */
        private val backPhotoResolutionPreference by lazy {
            ListPreference(context).apply {
                key = KEY_BACK_PHOTO_RESOLUTION
                setDefaultValue(DEF_VALUE_BACK_PHOTO_RESOLUTION)
                setTitle(R.string.back_photo_resolution_title)
                setEntriesAndEntryValues(CameraHelper.backDevice.photoResolutionSummaries)
                summaries = CameraHelper.backDevice.photoResolutionSummaries
                setDialogTitle(R.string.back_photo_resolution_title)
            }
        }

        /**
         * 前置摄像头组.
         */
        private val frontPreferenceGroup by lazy {
            if (!CameraHelper.hasFrontDevice) return@lazy null

            PreferenceGroup(context).apply {
                initPreferences(
                        frontIsPhotoPreference,
                        frontVideoPreferenceGroup,
                        frontPhotoPreferenceGroup)
                initIsGroupVisible { isFrontPreference.isChecked }
            }
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
            PreferenceGroup(context).apply {
                initPreferences(
                        frontVideoProfilePreferenceGroup)
                initIsGroupVisible { !frontIsPhotoPreference.isChecked }
            }
        }

        /**
         * 前置摄像头视频配置组.
         */
        private val frontVideoProfilePreferenceGroup by lazy {
            PreferenceGroup(context).apply {
                initPreferences(
                        frontVideoProfilePreference)
            }
        }

        /**
         * 前置摄像头视频配置.
         */
        private val frontVideoProfilePreference by lazy {
            ListPreference(context).apply {
                key = KEY_FRONT_VIDEO_PROFILE
                setDefaultValue(DEF_VALUE_FRONT_VIDEO_PROFILE)
                setTitle(R.string.front_video_profile_title)
                setEntriesAndEntryValues(CameraHelper.frontDevice.videoProfileSummaries)
                summaries = CameraHelper.frontDevice.videoProfileSummaries
                setDialogTitle(R.string.front_video_profile_title)
            }
        }

        /**
         * 前置摄像头照片组.
         */
        private val frontPhotoPreferenceGroup by lazy {
            PreferenceGroup(context).apply {
                initPreferences(
                        frontPhotoResolutionPreference)
                initIsGroupVisible { frontIsPhotoPreference.isChecked }
            }
        }

        /**
         * 前置摄像头照片分辨率.
         */
        private val frontPhotoResolutionPreference by lazy {
            ListPreference(context).apply {
                key = KEY_FRONT_PHOTO_RESOLUTION
                setDefaultValue(DEF_VALUE_FRONT_PHOTO_RESOLUTION)
                setTitle(R.string.front_photo_resolution_title)
                setEntriesAndEntryValues(CameraHelper.frontDevice.photoResolutionSummaries)
                summaries = CameraHelper.frontDevice.photoResolutionSummaries
                setDialogTitle(R.string.front_photo_resolution_title)
            }
        }

        /**
         * 预览.
         */
        private val previewPreference by lazy {
            ListPreference(context).apply {
                key = KEY_PREVIEW
                setDefaultValue(DEF_VALUE_PREVIEW)
                setTitle(R.string.preview_title)
                setEntriesAndEntryValues(Preview.entries)
                summaries = Preview.entries
                setDialogTitle(R.string.preview_title)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?) = arrayOf(
                devicePreferenceGroup,
                previewPreference)

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                KEY_IS_FRONT -> {
                    val newValue = isFront

                    backPreferenceGroup?.isGroupVisible = !newValue
                    frontPreferenceGroup?.isGroupVisible = newValue
                }
                KEY_BACK_IS_PHOTO -> {
                    val newValue = backIsPhoto

                    backVideoPreferenceGroup.isGroupVisible = !newValue
                    backPhotoPreferenceGroup.isGroupVisible = newValue
                }
                KEY_FRONT_IS_PHOTO -> {
                    val newValue = frontIsPhoto

                    frontVideoPreferenceGroup.isGroupVisible = !newValue
                    frontPhotoPreferenceGroup.isGroupVisible = newValue
                }
            }
        }
    }

    companion object {
        const val KEY_IS_FRONT = "is_front"
        const val KEY_BACK_IS_PHOTO = "back_is_photo"
        const val KEY_BACK_VIDEO_PROFILE = "back_video_profile"
        const val KEY_BACK_PHOTO_RESOLUTION = "back_photo_resolution"
        const val KEY_FRONT_IS_PHOTO = "front_is_photo"
        const val KEY_FRONT_VIDEO_PROFILE = "front_video_profile"
        const val KEY_FRONT_PHOTO_RESOLUTION = "front_photo_resolution"
        const val KEY_PREVIEW = "preview"

        // TODO: 暂时不允许只有单个摄像头的设备.
//        private val DEF_VALUE_IS_FRONT get() = !CameraHelper.hasBothDevices && CameraHelper.hasFrontDevice
        private const val DEF_VALUE_IS_FRONT = false
        private const val DEF_VALUE_BACK_IS_PHOTO = false
        private const val DEF_VALUE_BACK_VIDEO_PROFILE = "0"
        private const val DEF_VALUE_BACK_PHOTO_RESOLUTION = "0"
        private const val DEF_VALUE_FRONT_IS_PHOTO = false
        private const val DEF_VALUE_FRONT_VIDEO_PROFILE = "0"
        private const val DEF_VALUE_FRONT_PHOTO_RESOLUTION = "0"
        private val DEF_VALUE_PREVIEW = Preview.CAPTURE.indexString

        val isFront get() = sp.get(KEY_IS_FRONT, DEF_VALUE_IS_FRONT)
        val backIsPhoto get() = sp.get(KEY_BACK_IS_PHOTO, DEF_VALUE_BACK_IS_PHOTO)
        val backVideoProfile get() = sp.get(KEY_BACK_VIDEO_PROFILE, DEF_VALUE_BACK_VIDEO_PROFILE)
        val backPhotoResolution get() = sp.get(KEY_BACK_PHOTO_RESOLUTION,
                DEF_VALUE_BACK_PHOTO_RESOLUTION)
        val frontIsPhoto get() = sp.get(KEY_FRONT_IS_PHOTO, DEF_VALUE_FRONT_IS_PHOTO)
        val frontVideoProfile get() = sp.get(KEY_FRONT_VIDEO_PROFILE,
                DEF_VALUE_FRONT_VIDEO_PROFILE)
        val frontPhotoResolution get() = sp.get(KEY_FRONT_PHOTO_RESOLUTION,
                DEF_VALUE_FRONT_PHOTO_RESOLUTION)
        val preview get() = sp.get(KEY_PREVIEW, DEF_VALUE_PREVIEW)
    }
}
