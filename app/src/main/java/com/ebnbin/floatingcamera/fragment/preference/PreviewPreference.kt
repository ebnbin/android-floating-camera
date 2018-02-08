package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.Preview
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

/**
 * 预览偏好.
 */
class PreviewPreference(rootPreferenceGroup: RootPreferenceGroup) : ListPreference(rootPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.preview_title)
        dialogTitle = title
        setEntriesAndValues(Preview.entries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        rootPreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "preview_resolution"
        private val DEF_VALUE = Preview.CAPTURE.indexString

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
