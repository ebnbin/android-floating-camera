package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.support.v7.preference.PreferenceManager
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

open class ListPreference(context: Context, params: Params? = null) :
        android.support.v7.preference.ListPreference(context) {
    init {
        if (params?.key != null) key = params.key
        if (params?.defaultValue != null) setDefaultValue(params.defaultValue)
        if (params?.title != null) {
            title = params.title
            dialogTitle = title
        }
        if (params?.entries != null) setEntriesAndValues(params.entries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }
    }

    private var isFirstAttachedToHierarchy = true

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        if (isFirstAttachedToHierarchy) {
            isFirstAttachedToHierarchy = false

            summary = entry
        }
    }

    companion object {
        open class Params(
                val key: String? = null,
                val defaultValue: String? = null,
                val title: CharSequence? = null,
                val entries: Array<out CharSequence>? = null)
    }
}
