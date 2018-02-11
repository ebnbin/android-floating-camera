package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.event.IsDarkThemeEvent
import com.ebnbin.floatingcamera.util.Preview
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.getValueIndex
import com.ebnbin.floatingcamera.util.extension.getValueSize
import com.ebnbin.floatingcamera.util.getString
import org.greenrobot.eventbus.EventBus

/**
 * 偏好界面根偏好组.
 */
class RootPreferenceGroup(context: Context) : BaseRootPreferenceGroup(context),
        SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * 摄像头组.
     */
    private val devicePreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        isFrontPreference,
                        backPreferenceGroup,
                        frontPreferenceGroup)))
    }

    /**
     * 后置/前置摄像头.
     */
    private val isFrontPreference by lazy {
        SwitchPreference(context, SwitchPreference.Companion.Params(
                key = KEY_IS_FRONT,
                defaultValue = DEF_VALUE_IS_FRONT,
                isEnabled = cameraHelper.hasBothDevices,
                title = getString(R.string.is_front_title),
                summaryOff = getString(R.string.is_front_summary_off),
                summaryOn = getString(R.string.is_front_summary_on)))
    }

    /**
     * 后置摄像头组.
     */
    private val backPreferenceGroup by lazy {
        if (cameraHelper.hasBackDevice) {
            PreferenceGroup(context, PreferenceGroup.Companion.Params(
                    preferences = arrayOf(
                            backIsPhotoPreference,
                            backVideoPreferenceGroup,
                            backPhotoPreferenceGroup),
                    init = {
                        it.isGroupVisible = !isFrontPreference.isChecked
                    }))
        } else {
            null
        }
    }

    /**
     * 后置摄像头视频/照片.
     */
    private val backIsPhotoPreference by lazy {
        SwitchPreference(context, SwitchPreference.Companion.Params(
                key = KEY_BACK_IS_PHOTO,
                defaultValue = DEF_VALUE_BACK_IS_PHOTO,
                title = getString(R.string.is_photo_title),
                summaryOff = getString(R.string.back_is_photo_summary_off),
                summaryOn = getString(R.string.back_is_photo_summary_on)))
    }

    /**
     * 后置摄像头视频组.
     */
    private val backVideoPreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        backVideoProfilePreferenceGroup),
                init = {
                    it.isGroupVisible = !backIsPhotoPreference.isChecked
                }))
    }

    /**
     * 后置摄像头视频配置组.
     */
    private val backVideoProfilePreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        backVideoProfilePreference,
                        backVideoProfileCustomPreferenceGroup)))
    }

    /**
     * 后置摄像头视频配置.
     */
    private val backVideoProfilePreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_BACK_VIDEO_PROFILE,
                defaultValue = DEF_VALUE_BACK_VIDEO_PROFILE,
                title = getString(R.string.back_video_profile_title),
                entries = cameraHelper.backDevice.videoProfileSummaries))
    }

    /**
     * 后置摄像头视频自定义配置组.
     */
    private val backVideoProfileCustomPreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        backVideoResolutionPreference),
                init = {
                    it.isGroupVisible = backVideoProfilePreference.getValueIndex() ==
                            backVideoProfilePreference.getValueSize() - 1
                }))
    }

    /**
     * 后置摄像头视频分辨率.
     */
    private val backVideoResolutionPreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_BACK_VIDEO_RESOLUTION,
                defaultValue = DEF_VALUE_BACK_VIDEO_RESOLUTION,
                title = getString(R.string.back_video_resolution_title),
                entries = cameraHelper.backDevice.videoResolutionSummaries))
    }

    /**
     * 后置摄像头照片组.
     */
    private val backPhotoPreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        backPhotoResolutionPreference),
                init = {
                    it.isGroupVisible = backIsPhotoPreference.isChecked
                }))
    }

    /**
     * 后置摄像头照片分辨率.
     */
    private val backPhotoResolutionPreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_BACK_PHOTO_RESOLUTION,
                defaultValue = DEF_VALUE_BACK_PHOTO_RESOLUTION,
                title = getString(R.string.back_photo_resolution_title),
                entries = cameraHelper.backDevice.photoResolutionSummaries))
    }

    /**
     * 前置摄像头组.
     */
    private val frontPreferenceGroup by lazy {
        if (cameraHelper.hasFrontDevice) {
            PreferenceGroup(context, PreferenceGroup.Companion.Params(
                    preferences = arrayOf(
                            frontIsPhotoPreference,
                            frontVideoPreferenceGroup,
                            frontPhotoPreferenceGroup),
                    init = {
                        it.isGroupVisible = isFrontPreference.isChecked
                    }))
        } else {
            null
        }
    }

    /**
     * 前置摄像头视频/照片.
     */
    private val frontIsPhotoPreference by lazy {
        SwitchPreference(context, SwitchPreference.Companion.Params(
                key = KEY_FRONT_IS_PHOTO,
                defaultValue = DEF_VALUE_FRONT_IS_PHOTO,
                title = getString(R.string.is_photo_title),
                summaryOff = getString(R.string.front_is_photo_summary_off),
                summaryOn = getString(R.string.front_is_photo_summary_on)))
    }

    /**
     * 前置摄像头视频组.
     */
    private val frontVideoPreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        frontVideoProfilePreferenceGroup),
                init = {
                    it.isGroupVisible = !frontIsPhotoPreference.isChecked
                }))
    }

    /**
     * 前置摄像头视频配置组.
     */
    private val frontVideoProfilePreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        frontVideoProfilePreference,
                        frontVideoProfileCustomPreferenceGroup)))
    }

    /**
     * 前置摄像头视频配置.
     */
    private val frontVideoProfilePreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_FRONT_VIDEO_PROFILE,
                defaultValue = DEF_VALUE_FRONT_VIDEO_PROFILE,
                title = getString(R.string.front_video_profile_title),
                entries = cameraHelper.frontDevice.videoProfileSummaries))
    }

    /**
     * 前置摄像头视频自定义配置组.
     */
    private val frontVideoProfileCustomPreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        frontVideoResolutionPreference),
                init = {
                    it.isGroupVisible = frontVideoProfilePreference.getValueIndex() ==
                            frontVideoProfilePreference.getValueSize() - 1
                }))
    }

    /**
     * 前置摄像头视频分辨率.
     */
    private val frontVideoResolutionPreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_FRONT_VIDEO_RESOLUTION,
                defaultValue = DEF_VALUE_FRONT_VIDEO_RESOLUTION,
                title = getString(R.string.front_video_resolution_title),
                entries = cameraHelper.frontDevice.videoResolutionSummaries))
    }

    /**
     * 前置摄像头照片组.
     */
    private val frontPhotoPreferenceGroup by lazy {
        PreferenceGroup(context, PreferenceGroup.Companion.Params(
                preferences = arrayOf(
                        frontPhotoResolutionPreference),
                init = {
                    it.isGroupVisible = frontIsPhotoPreference.isChecked
                }))
    }

    /**
     * 前置摄像头照片分辨率.
     */
    private val frontPhotoResolutionPreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_FRONT_PHOTO_RESOLUTION,
                defaultValue = DEF_VALUE_FRONT_PHOTO_RESOLUTION,
                title = getString(R.string.front_photo_resolution_title),
                entries = cameraHelper.frontDevice.photoResolutionSummaries))
    }

    /**
     * 预览.
     */
    private val previewPreference by lazy {
        ListPreference(context, ListPreference.Companion.Params(
                key = KEY_PREVIEW_RESOLUTION,
                defaultValue = DEF_VALUE_PREVIEW_RESOLUTION,
                title = getString(R.string.preview_title),
                entries = Preview.entries))
    }

    /**
     * 悬浮窗大小.
     */
    private val windowSizePreference by lazy {
        SeekBarPreference(context, SeekBarPreference.Companion.Params(
                key = KEY_WINDOW_SIZE,
                defaultValue = DEF_VALUE_WINDOW_SIZE,
                title = getString(R.string.window_size_title),
                summary = getString(R.string.window_size_summary),
                min = 0,
                max = 100))
    }

    /**
     * 悬浮窗水平方向位置.
     */
    private val windowXPreference by lazy {
        SeekBarPreference(context, SeekBarPreference.Companion.Params(
                key = KEY_WINDOW_X,
                defaultValue = DEF_VALUE_WINDOW_X,
                title = getString(R.string.window_x_title),
                summary = getString(R.string.window_x_summary),
                min = -100,
                max = 200))
    }

    /**
     * 悬浮窗垂直方向位置.
     */
    private val windowYPreference by lazy {
        SeekBarPreference(context, SeekBarPreference.Companion.Params(
                key = KEY_WINDOW_Y,
                defaultValue = DEF_VALUE_WINDOW_Y,
                title = getString(R.string.window_y_title),
                summary = getString(R.string.window_y_summary),
                min = -100,
                max = 200))
    }

    /**
     * 主题.
     */
    private val isDarkThemePreference by lazy {
        SwitchPreference(context, SwitchPreference.Companion.Params(
                key = KEY_IS_DARK_THEME,
                defaultValue = DEF_VALUE_IS_DARK_THEME,
                title = getString(R.string.is_dark_theme_title),
                summaryOff = getString(R.string.is_dark_theme_summary_off),
                summaryOn = getString(R.string.is_dark_theme_summary_on)))
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        addPreferenceToGroup(devicePreferenceGroup)
        addPreferenceToGroup(previewPreference)
        addPreferenceToGroup(windowSizePreference)
        addPreferenceToGroup(windowXPreference)
        addPreferenceToGroup(windowYPreference)
        addPreferenceToGroup(isDarkThemePreference)
    }

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
            KEY_IS_DARK_THEME -> {
                EventBus.getDefault().post(IsDarkThemeEvent())
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
        private const val KEY_PREVIEW_RESOLUTION = "preview_resolution"
        private const val KEY_WINDOW_SIZE = "window_size"
        private const val KEY_WINDOW_X = "window_x"
        private const val KEY_WINDOW_Y = "window_y"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"

        private val DEF_VALUE_IS_FRONT get() = !cameraHelper.hasBothDevices && cameraHelper.hasFrontDevice
        private const val DEF_VALUE_BACK_IS_PHOTO = false
        private const val DEF_VALUE_BACK_VIDEO_PROFILE = "0"
        private const val DEF_VALUE_BACK_VIDEO_RESOLUTION = "0"
        private const val DEF_VALUE_BACK_PHOTO_RESOLUTION = "0"
        private const val DEF_VALUE_FRONT_IS_PHOTO = false
        private const val DEF_VALUE_FRONT_VIDEO_PROFILE = "0"
        private const val DEF_VALUE_FRONT_VIDEO_RESOLUTION = "0"
        private const val DEF_VALUE_FRONT_PHOTO_RESOLUTION = "0"
        private val DEF_VALUE_PREVIEW_RESOLUTION = Preview.CAPTURE.indexString
        private const val DEF_VALUE_WINDOW_SIZE = 50
        private const val DEF_VALUE_WINDOW_X = 50
        private const val DEF_VALUE_WINDOW_Y = 50
        private const val DEF_VALUE_IS_DARK_THEME = false

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
        val previewResolution get() = defaultSharedPreferences.get(KEY_PREVIEW_RESOLUTION,
                DEF_VALUE_PREVIEW_RESOLUTION)
        val windowSize get() = defaultSharedPreferences.get(KEY_WINDOW_SIZE, DEF_VALUE_WINDOW_SIZE)
        val windowX get() = defaultSharedPreferences.get(KEY_WINDOW_X, DEF_VALUE_WINDOW_X)
        val windowY get() = defaultSharedPreferences.get(KEY_WINDOW_Y, DEF_VALUE_WINDOW_Y)
        val isDarkTheme get() = defaultSharedPreferences.get(KEY_IS_DARK_THEME, DEF_VALUE_IS_DARK_THEME)
    }
}
