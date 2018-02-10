package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.PreferenceGroup
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

open class ListPreference(params: Params) :
        android.support.v7.preference.ListPreference(params.preferenceGroup.context) {
    init {
        if (params.key != null) key = params.key
        if (params.defaultValue != null) setDefaultValue(params.defaultValue)
        if (params.title != null) {
            title = params.title
            dialogTitle = title
        }
        if (params.entries != null) setEntriesAndValues(params.entries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        if (params.preferenceGroup is BasePreferenceGroup) {
            params.preferenceGroup.addPreferenceToGroup(this)
        } else {
            params.preferenceGroup.addPreference(this)
        }

        if (params.defaultValue != null && params.entries != null) summary = entry
    }

    companion object {
        open class Params(
                val preferenceGroup: PreferenceGroup,
                val key: String? = null,
                val defaultValue: String? = null,
                val title: CharSequence? = null,
                val entries: Array<out CharSequence>? = null)
    }
}
