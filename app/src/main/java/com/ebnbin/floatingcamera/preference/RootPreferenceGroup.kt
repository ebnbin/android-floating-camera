package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference

/**
 * 基础根偏好组.
 */
abstract class RootPreferenceGroup(context: Context) : PreferenceGroup(context),
        SharedPreferences.OnSharedPreferenceChangeListener {
    val contentPreferenceGroup by lazy {
        PreferenceGroup(context)
    }

    /**
     * 底部偏好.
     */
    val footerPreference by lazy {
        FooterPreference(context)
    }

    fun createPreferences(savedInstanceState: Bundle?) {
        initPreferences(
                contentPreferenceGroup.apply {
                    initPreferences(*onCreatePreferences(savedInstanceState))
                },
                footerPreference)
    }

    protected abstract fun onCreatePreferences(savedInstanceState: Bundle?): Array<out Preference>

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) = Unit
}
