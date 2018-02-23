package com.ebnbin.floatingcamera.fragment.preference.other

import android.content.Context
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.FooterPreference
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.preference.SwitchPreference
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.getString

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
                        isExternalPreference,
                        isDarkThemePreference))
    }

    /**
     * 存储路径.
     */
    private val isExternalPreference by lazy {
        SwitchPreference(context,
                key = KEY_IS_EXTERNAL,
                defaultValue = DEF_VALUE_IS_EXTERNAL,
                isEnabled = FileUtil.isInternalAvailable,
                title = getString(R.string.is_external_title),
                summaryOff = context.getString(R.string.is_external_summary_off, FileUtil.internalPath),
                summaryOn = context.getString(R.string.is_external_summary_on, FileUtil.externalPath))
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
        const val KEY_IS_EXTERNAL = "is_external"
        const val KEY_IS_DARK_THEME = "is_dark_theme"

        private val DEF_VALUE_IS_EXTERNAL = !FileUtil.isInternalAvailable
        private const val DEF_VALUE_IS_DARK_THEME = false

        val isExternal get() = defaultSharedPreferences.get(KEY_IS_EXTERNAL, DEF_VALUE_IS_EXTERNAL)
        val isDarkTheme get() = defaultSharedPreferences.get(KEY_IS_DARK_THEME, DEF_VALUE_IS_DARK_THEME)
    }
}
