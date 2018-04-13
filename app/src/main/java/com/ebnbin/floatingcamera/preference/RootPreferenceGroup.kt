package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.content.SharedPreferences

/**
 * 基础根偏好组.
 */
abstract class RootPreferenceGroup(context: Context) : PreferenceGroup(context),
        SharedPreferences.OnSharedPreferenceChangeListener{
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) = Unit
}
