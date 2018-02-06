package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceScreen
import com.ebnbin.floatingcamera.R

/**
 * 底部偏好.
 */
class FooterPreference(preferenceScreen: PreferenceScreen) : Preference(preferenceScreen.context) {
    init {
        isEnabled = false
        layoutResource = R.layout.footer_preference

        preferenceScreen.addPreference(this)
    }
}
