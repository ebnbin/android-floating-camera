package com.ebnbin.floatingcamera.util.extension

import android.support.v7.preference.ListPreference

/**
 * 返回 [ListPreference.mEntryValues] 数量.
 */
fun ListPreference.getValueSize() = entryValues.size

/**
 * [ListPreference.getValueIndex].
 */
fun ListPreference.getValueIndex() = findIndexOfValue(value)

fun ListPreference.setEntriesAndEntryValues(entries: Array<out CharSequence>?) {
    this.entries = entries
    entryValues = if (this.entries == null) null else Array(this.entries.size) { it.toString() }
}
