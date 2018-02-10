package com.ebnbin.floatingcamera.fragment.preference

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
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
 *         PreviewPreference
 *         WindowSizePreference
 *         windowXPreference
 *         windowYPreference
 *         IsDarkThemePreference
 *     FooterPreference
 */
class PreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var rootPreferenceGroup: RootPreferenceGroup

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        rootPreferenceGroup = RootPreferenceGroup(preferenceScreen)
        FooterPreference(preferenceScreen)

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(rootPreferenceGroup)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(rootPreferenceGroup)

        super.onDestroy()
    }
}
