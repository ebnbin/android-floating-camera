package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import com.ebnbin.floatingcamera.BuildConfig
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.PreferenceFragment
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.res
import com.ebnbin.floatingcamera.util.sp

/**
 * 其他偏好界面.
 */
class OtherPreferenceFragment : PreferenceFragment<OtherPreferenceFragment.OtherRootPreferenceGroup>() {
    override fun createRootPreferenceGroup(context: Context) = OtherRootPreferenceGroup(context)

    /**
     *     PathPreference
     *     IsDarkThemePreference
     */
    class OtherRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
        /**
         * 存储路径.
         */
        private val pathPreference by lazy {
            Preference(context).apply {
                setTitle(R.string.path_title)
                summary = res.getString(R.string.path_summary, FileUtil.getPath())
            }
        }

        /**
         * 主题.
         */
        private val isDarkThemePreference by lazy {
            SwitchPreference(context).apply {
                key = KEY_IS_DARK_THEME
                setDefaultValue(DEF_VALUE_IS_DARK_THEME)
                setTitle(R.string.is_dark_theme_title)
                setSummaryOff(R.string.is_dark_theme_summary_off)
                setSummaryOn(R.string.is_dark_theme_summary_on)
            }
        }

        private val aboutPreference by lazy {
            Preference(context).apply {
                setTitle(R.string.about_title)
                summary = res.getString(R.string.about_summary, BuildConfig.VERSION_NAME)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?) = arrayOf(
                pathPreference,
                isDarkThemePreference,
                aboutPreference)
    }

    companion object {
        const val KEY_IS_DARK_THEME = "is_dark_theme"

        private const val DEF_VALUE_IS_DARK_THEME = false

        val isDarkTheme get() = sp.get(KEY_IS_DARK_THEME, DEF_VALUE_IS_DARK_THEME)
    }
}
