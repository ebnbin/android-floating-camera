package com.ebnbin.floatingcamera.fragment.preference.other

import android.content.Context
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.FooterPreference
import com.ebnbin.floatingcamera.preference.Preference
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.preference.SwitchPreference
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.getString
import com.ebnbin.floatingcamera.util.resources

/**
 * 其他根偏好组.
 *
 *     OtherPreferenceGroup
 *         IsDarkThemePreference
 *     FooterPreference
 */
class OtherRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
    /**
     * 其他偏好组.
     */
    private val otherPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        pathPreference,
                        isDarkThemePreference))
    }

    /**
     * 存储路径.
     */
    private val pathPreference by lazy {
        Preference(context,
                isEnabled = true,
                layoutResource = null,
                title = getString(R.string.path_title),
                summary = resources.getString(R.string.path_summary, FileUtil.path))
    }

    /**
     * 主题.
     */
    private val isDarkThemePreference by lazy {
        SwitchPreference(context,
                key = KEY_IS_DARK_THEME,
                defaultValue = DEF_VALUE_IS_DARK_THEME,
                isEnabled = null,
                title = getString(R.string.is_dark_theme_title),
                summaryOff = getString(R.string.is_dark_theme_summary_off),
                summaryOn = getString(R.string.is_dark_theme_summary_on))
    }

    /**
     * 底部偏好.
     */
    private val footerPreference by lazy {
        FooterPreference(context)
    }

    override fun preferences() = arrayOf(
            otherPreferenceGroup,
            footerPreference)

    companion object {
        const val KEY_IS_DARK_THEME = "is_dark_theme"

        private const val DEF_VALUE_IS_DARK_THEME = false

        val isDarkTheme get() = defaultSharedPreferences.get(KEY_IS_DARK_THEME, DEF_VALUE_IS_DARK_THEME)
    }
}
