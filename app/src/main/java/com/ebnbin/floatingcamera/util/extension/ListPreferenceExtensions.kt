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
