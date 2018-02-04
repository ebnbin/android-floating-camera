package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceGroup
import com.ebnbin.floatingcamera.R

/**
 * 底部偏好.
 */
class FooterPreference(preferenceGroup: PreferenceGroup) : Preference(preferenceGroup.context) {
    init {
        isEnabled = false
        layoutResource = R.layout.footer_preference

        preferenceGroup.addPreference(this)
    }
}
