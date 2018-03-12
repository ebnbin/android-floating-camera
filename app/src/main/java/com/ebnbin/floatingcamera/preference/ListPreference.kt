package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.support.v7.preference.PreferenceManager

/**
 * [android.support.v7.preference.ListPreference].
 */
open class ListPreference(context: Context) : android.support.v7.preference.ListPreference(context) {
    var summaries: Array<out CharSequence>? = null
        set(value) {
            field = value

            if (field != null) {
                onPreferenceChangeListener = superOnPreferenceChangeListener
            }
        }

    private var superOnPreferenceChangeListener: OnPreferenceChangeListener? = null

    override fun setOnPreferenceChangeListener(onPreferenceChangeListener: OnPreferenceChangeListener?) {
        superOnPreferenceChangeListener = onPreferenceChangeListener

        super.setOnPreferenceChangeListener { preference, newValue ->
            val result = superOnPreferenceChangeListener?.onPreferenceChange(preference, newValue)

            newValue as String
            invalidateSummary(newValue)

            result ?: true
        }
    }

    override fun getOnPreferenceChangeListener() = superOnPreferenceChangeListener

    private var isFirstAttachedToHierarchy = true

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        if (!isFirstAttachedToHierarchy) return

        isFirstAttachedToHierarchy = false

        invalidateSummary()
    }

    /**
     * 更新摘要.
     */
    private fun invalidateSummary(value: String = this.value) {
        if (summaries == null) return

        val index = getValueIndex(value)
        summary = if (index in 0 until (summaries?.size ?: 0)) summaries!![index] else null
    }

    private fun getValueIndex(value: String = this.value) = findIndexOfValue(value)

    @Deprecated("使用 setEntriesAndEntryValues 代替.", ReplaceWith("setEntriesAndEntryValues(entries)",
            "com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues"))
    override fun setEntries(entries: Array<out CharSequence>?) = super.setEntries(entries)

    @Deprecated("使用 setEntriesAndEntryValues 代替.", ReplaceWith("setEntriesAndEntryValues(entries)",
            "com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues"))
    override fun setEntries(entriesResId: Int) = super.setEntries(entriesResId)

    @Deprecated("使用 setEntriesAndEntryValues 代替.", ReplaceWith("setEntriesAndEntryValues(entries)",
            "com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues"))
    override fun setEntryValues(entryValues: Array<out CharSequence>?) = super.setEntryValues(entryValues)

    @Deprecated("使用 setEntriesAndEntryValues 代替.", ReplaceWith("setEntriesAndEntryValues(entries)",
            "com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues"))
    override fun setEntryValues(entryValuesResId: Int) = super.setEntryValues(entryValuesResId)
}
