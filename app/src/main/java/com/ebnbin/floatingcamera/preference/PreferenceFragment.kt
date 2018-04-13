package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.ebnbin.floatingcamera.R

/**
 * 基础偏好界面.
 */
abstract class PreferenceFragment<T : RootPreferenceGroup> : PreferenceFragmentCompat() {
    protected lateinit var rootPreferenceGroup: T
        private set

    protected abstract fun createRootPreferenceGroup(context: Context): T

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        rootPreferenceGroup = createRootPreferenceGroup(preferenceScreen.context)
        rootPreferenceGroup.createPreferences(savedInstanceState)
        preferenceScreen.addPreference(rootPreferenceGroup)

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(rootPreferenceGroup)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(rootPreferenceGroup)

        super.onDestroy()
    }
}
