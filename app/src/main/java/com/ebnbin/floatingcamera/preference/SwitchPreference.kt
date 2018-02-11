package com.ebnbin.floatingcamera.preference

import android.content.Context

/**
 * [android.support.v14.preference.SwitchPreference].
 */
open class SwitchPreference(
        context: Context,
        key: String? = null,
        defaultValue: Boolean? = null,
        isEnabled: Boolean? = null,
        title: CharSequence? = null,
        summaryOff: CharSequence? = null,
        summaryOn: CharSequence? = null) :
        android.support.v14.preference.SwitchPreference(context) {
    init {
        if (key != null) this.key = key
        if (defaultValue != null) super.setDefaultValue(defaultValue)
        if (isEnabled != null) this.isEnabled = isEnabled
        if (title != null) this.title = title
        if (summaryOff != null) this.summaryOff = summaryOff
        if (summaryOn != null) this.summaryOn = summaryOn
    }
}
