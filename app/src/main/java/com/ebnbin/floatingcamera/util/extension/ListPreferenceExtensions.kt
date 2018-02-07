package com.ebnbin.floatingcamera.util.extension

import android.support.v7.preference.ListPreference

/**
 * 设置 [ListPreference.mEntries] 并以 index 字符串设置 [ListPreference.mEntryValues].
 */
fun ListPreference.setEntriesAndEntryValues(entries: Array<out CharSequence>) {
    this.entries = entries
    entryValues = Array(this.entries.size) { it.toString() }
}
