package com.ebnbin.floatingcamera.fragment.preference.other

import android.content.Context
import android.content.SharedPreferences
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.event.IsDarkThemeEvent
import com.ebnbin.floatingcamera.preference.FooterPreference
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.preference.SwitchPreference
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.getString
import org.greenrobot.eventbus.EventBus

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
                        isDarkThemePreference))
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_IS_DARK_THEME -> {
                EventBus.getDefault().post(IsDarkThemeEvent())
            }
        }
    }

    companion object {
        private const val KEY_IS_DARK_THEME = "is_dark_theme"

        private const val DEF_VALUE_IS_DARK_THEME = false

        val isDarkTheme get() = defaultSharedPreferences.get(KEY_IS_DARK_THEME, DEF_VALUE_IS_DARK_THEME)
    }
}
