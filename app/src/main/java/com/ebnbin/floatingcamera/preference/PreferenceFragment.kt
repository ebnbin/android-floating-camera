package com.ebnbin.floatingcamera.preference

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceScreen
import com.ebnbin.floatingcamera.R

/**
 * 基础偏好界面.
 */
abstract class PreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var rootPreferenceGroup: RootPreferenceGroup

    protected abstract fun createRootPreferenceGroup(preferenceScreen: PreferenceScreen): RootPreferenceGroup

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        rootPreferenceGroup = createRootPreferenceGroup(preferenceScreen)
        preferenceScreen.addPreference(rootPreferenceGroup)

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(rootPreferenceGroup)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(rootPreferenceGroup)

        super.onDestroy()
    }
}
