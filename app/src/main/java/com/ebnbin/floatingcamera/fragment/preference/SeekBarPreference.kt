package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context

open class SeekBarPreference(context: Context, params: Params? = null) :
        android.support.v7.preference.SeekBarPreference(context) {
    init {
        if (params?.key != null) key = params.key
        if (params?.defaultValue != null) setDefaultValue(params.defaultValue)
        if (params?.title != null) title = params.title
        if (params?.summary != null) summary = params.summary
        if (params?.min != null) min = params.min
        if (params?.max != null) max = params.max
    }

    companion object {
        open class Params(
                val key: String? = null,
                val defaultValue: Int? = null,
                val title: CharSequence? = null,
                val summary: CharSequence? = null,
                val min: Int? = null,
                val max: Int? = null)
    }
}
