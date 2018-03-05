package com.ebnbin.floatingcamera.preference

import android.content.Context
import com.ebnbin.floatingcamera.R

/**
 * 底部偏好.
 */
class FooterPreference(context: Context) : Preference(context,
        isEnabled = false,
        layoutResource = R.layout.footer_preference,
        title = null,
        summary = null)
