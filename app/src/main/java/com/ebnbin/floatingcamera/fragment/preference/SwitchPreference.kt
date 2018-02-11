package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context

open class SwitchPreference(context: Context, params: Params? = null) :
        android.support.v14.preference.SwitchPreference(context) {
    init {
        if (params?.key != null) key = params.key
        if (params?.defaultValue != null) setDefaultValue(params.defaultValue)
        if (params?.isEnabled != null) isEnabled = params.isEnabled
        if (params?.title != null) title = params.title
        if (params?.summaryOff != null) summaryOff = params.summaryOff
        if (params?.summaryOn != null) summaryOn = params.summaryOn
    }

    companion object {
        open class Params(
                val key: String? = null,
                val defaultValue: Boolean? = null,
                val isEnabled: Boolean? = null,
                val title: CharSequence? = null,
                val summaryOff: CharSequence? = null,
                val summaryOn: CharSequence? = null)
    }
}
