package com.ebnbin.floatingcamera.util

import android.util.Log
import com.ebnbin.floatingcamera.BuildConfig

/**
 * Debug 帮助类.
 */
object DebugHelper {
    private const val TAG = "ebnbin"

    fun log(any: Any?, key: String? = null) {
        if (!BuildConfig.DEBUG) return

        if (any is Throwable) {
            Log.e(TAG, key ?: "", any)
        } else {
            Log.d(TAG, "${if (key == null) "" else "$key="}$any")
        }
    }
}
