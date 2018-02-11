package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.Preference

/**
 * 基础根偏好组.
 */
abstract class RootPreferenceGroup(context: Context) : PreferenceGroup(context),
        SharedPreferences.OnSharedPreferenceChangeListener{
    abstract override fun preferences(): Array<out Preference?>?
}
