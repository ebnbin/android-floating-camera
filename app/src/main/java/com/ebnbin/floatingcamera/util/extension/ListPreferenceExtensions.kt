package com.ebnbin.floatingcamera.util.extension

import android.support.v7.preference.ListPreference

/**
 * 设置 [ListPreference.mEntries] 并以 index 字符串设置 [ListPreference.mEntryValues].
 */
fun ListPreference.setEntriesAndValues(entries: Array<out CharSequence>) {
    this.entries = entries
    entryValues = Array(this.entries.size) { it.toString() }
}

/**
 * 返回 [ListPreference.mEntryValues] 数量.
 */
fun ListPreference.getValueSize() = entryValues.size

/**
 * [ListPreference.getValueIndex].
 */
fun ListPreference.getValueIndex() = findIndexOfValue(value)
