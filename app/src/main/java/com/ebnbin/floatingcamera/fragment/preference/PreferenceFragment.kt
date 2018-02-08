package com.ebnbin.floatingcamera.fragment.preference

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceGroup
import com.ebnbin.floatingcamera.R

/**
 * 偏好界面.
 *
 *     RootPreferenceGroup
 *         DevicePreferenceGroup
 *             IsFrontPreference
 *             BackPreferenceGroup?
 *                 BackIsPhotoPreference
 *                 BackVideoPreferenceGroup
 *                     BackVideoProfilePreferenceGroup
 *                         BackVideoProfilePreference
 *                         BackVideoProfileCustomPreferenceGroup
 *                             BackVideoResolutionPreference
 *                 BackPhotoPreferenceGroup
 *                     BackPhotoResolutionPreference
 *             FrontPreferenceGroup?
 *                 FrontIsPhotoPreference
 *                 FrontVideoPreferenceGroup
 *                     FrontVideoProfilePreferenceGroup
 *                         FrontVideoProfilePreference
 *                         FrontVideoProfileCustomPreferenceGroup
 *                             FrontVideoResolutionPreference
 *                 FrontPhotoPreferenceGroup
 *                     FrontPhotoResolutionPreference
 *         IsDarkThemePreference
 *     FooterPreference
 */
class PreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var rootPreferenceGroup: RootPreferenceGroup

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        rootPreferenceGroup = RootPreferenceGroup(preferenceScreen)
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
        /**
         * 反注册 [SharedPreferences.OnSharedPreferenceChangeListener]. 如果为 [PreferenceGroup], 调用
         * [unregisterOnSharedPreferenceChangeListeners] 递归.
         */
        fun unregisterOnSharedPreferenceChangeListener(preference: Preference) {
            if (preference is SharedPreferences.OnSharedPreferenceChangeListener) {
                preference.sharedPreferences.unregisterOnSharedPreferenceChangeListener(preference)
            }
            if (preference is PreferenceGroup) {
                unregisterOnSharedPreferenceChangeListeners(preference)
            }
        }

        if (preferenceGroup is BasePreferenceGroup) {
            for (preference in preferenceGroup.preferences) {
                unregisterOnSharedPreferenceChangeListener(preference)
            }
        } else {
            for (index in 0 until preferenceGroup.preferenceCount) {
                unregisterOnSharedPreferenceChangeListener(preferenceGroup.getPreference(index))
            }
        }
    }
}
