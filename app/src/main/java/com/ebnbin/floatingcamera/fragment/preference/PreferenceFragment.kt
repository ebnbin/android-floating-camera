package com.ebnbin.floatingcamera.fragment.preference

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.ebnbin.floatingcamera.R

/**
 * 偏好界面.
 */
class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        preferenceScreen.addPreference(FooterPreference(preferenceScreen.context))
    }
}
