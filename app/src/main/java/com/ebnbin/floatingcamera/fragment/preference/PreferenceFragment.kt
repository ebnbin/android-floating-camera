package com.ebnbin.floatingcamera.fragment.preference

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceGroup
import com.ebnbin.floatingcamera.R

/**
 * 偏好界面.
 */
class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        IsDarkThemePreference(preferenceScreen)
        FooterPreference(preferenceScreen)
    }

    override fun onDestroy() {
        unregisterOnSharedPreferenceChangeListeners()

        super.onDestroy()
    }

    /**
     * 反注册所有 [SharedPreferences.OnSharedPreferenceChangeListener].
     */
    private fun unregisterOnSharedPreferenceChangeListeners(preferenceGroup: PreferenceGroup = preferenceScreen) {
        (0 until preferenceGroup.preferenceCount)
                .map { preferenceGroup.getPreference(it) }
                .forEach {
                    if (it is SharedPreferences.OnSharedPreferenceChangeListener) {
                        it.sharedPreferences.unregisterOnSharedPreferenceChangeListener(it)
                    }
                    if (it is PreferenceGroup) {
                        unregisterOnSharedPreferenceChangeListeners(it)
                    }
                }
    }
}
