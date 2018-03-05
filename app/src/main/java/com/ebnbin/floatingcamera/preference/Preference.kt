package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.support.annotation.LayoutRes

/**
 * [android.support.v7.preference.Preference].
 */
open class Preference(
        context: Context,
        isEnabled: Boolean? = null,
        @LayoutRes layoutResource: Int? = null,
        title: CharSequence? = null,
        summary: CharSequence? = null) :
        android.support.v7.preference.Preference(context) {
    init {
        if (isEnabled != null) this.isEnabled = isEnabled
        if (layoutResource != null) this.layoutResource = layoutResource
        if (title != null) this.title = title
        if (summary != null) this.summary = summary
    }
}
