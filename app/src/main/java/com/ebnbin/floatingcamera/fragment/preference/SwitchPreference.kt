package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.PreferenceGroup

open class SwitchPreference(params: Params) :
        android.support.v14.preference.SwitchPreference(params.preferenceGroup.context) {
    init {
        if (params.key != null) key = params.key
        if (params.defaultValue != null) setDefaultValue(params.defaultValue)
        if (params.isEnabled != null) isEnabled = params.isEnabled
        if (params.title != null) title = params.title
        if (params.summaryOff != null) summaryOff = params.summaryOff
        if (params.summaryOn != null) summaryOn = params.summaryOn

        if (params.preferenceGroup is BasePreferenceGroup) {
            params.preferenceGroup.addPreferenceToGroup(this)
        } else {
            params.preferenceGroup.addPreference(this)
        }
    }

    companion object {
        open class Params(
                val preferenceGroup: PreferenceGroup,
                val key: String? = null,
                val defaultValue: Boolean? = null,
                val isEnabled: Boolean? = null,
                val title: CharSequence? = null,
                val summaryOff: CharSequence? = null,
                val summaryOn: CharSequence? = null)
    }
}
