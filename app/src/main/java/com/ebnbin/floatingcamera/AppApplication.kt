package com.ebnbin.floatingcamera

import android.app.Application
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.PreferenceHelper

/**
 * 应用 [Application] 类.
 */
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        singleton = this

        PreferenceHelper
    }

    companion object {
        private var singleton: AppApplication? = null

        val instance get() = singleton ?: throw BaseRuntimeException()
    }
}
