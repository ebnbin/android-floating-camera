package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.support.v7.preference.PreferenceManager

/**
 * [android.support.v7.preference.ListPreference].
 */
open class ListPreference(
        context: Context,
        key: String? = null,
        defaultValue: String? = null,
        title: CharSequence? = null,
        entries: Array<out CharSequence>? = null,
        private val summaries: Array<out CharSequence>? = null,
        dialogTitle: CharSequence? = null) :
        android.support.v7.preference.ListPreference(context) {
    init {
        if (key != null) this.key = key
        if (defaultValue != null) super.setDefaultValue(defaultValue)
        if (title != null) this.title = title
        if (entries != null) {
            this.entries = entries
            entryValues = Array(this.entries.size) { it.toString() }
            if (summaries != null) {
                setOnPreferenceChangeListener { _, newValue ->
                    newValue as String

                    invalidateSummary(newValue)

                    true
                }
            }
        }
        if (dialogTitle != null) this.dialogTitle = dialogTitle
    }

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
        summary = if (index in 0 until summaries.size) summaries[index] else null
    }

    private fun getValueIndex(value: String = this.value) = findIndexOfValue(value)
}
