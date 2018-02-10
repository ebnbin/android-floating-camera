package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.PreferenceGroup

open class SeekBarPreference(params: Params) :
        android.support.v7.preference.SeekBarPreference(params.preferenceGroup.context) {
    init {
        if (params.key != null) key = params.key
        if (params.defaultValue != null) setDefaultValue(params.defaultValue)
        if (params.title != null) title = params.title
        if (params.summary != null) summary = params.summary
        if (params.min != null) min = params.min
        if (params.max != null) max = params.max

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
                val defaultValue: Int? = null,
                val title: CharSequence? = null,
                val summary: CharSequence? = null,
                val min: Int? = null,
                val max: Int? = null)
    }
}
