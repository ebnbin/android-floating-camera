package com.ebnbin.floatingcamera.preference

import android.content.Context

/**
 * [android.support.v7.preference.SeekBarPreference].
 */
class SeekBarPreference(
        context: Context,
        key: String? = null,
        defaultValue: Int? = null,
        title: CharSequence? = null,
        summary: CharSequence? = null,
        min: Int? = null,
        max: Int? = null) :
        android.support.v7.preference.SeekBarPreference(context) {
    init {
        if (key != null) this.key = key
        if (defaultValue != null) setDefaultValue(defaultValue)
        if (title != null) this.title = title
        if (summary != null) this.summary = summary
        if (min != null) this.min = min
        if (max != null) this.max = max
    }
}
