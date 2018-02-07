package com.ebnbin.floatingcamera

import android.app.Application
import com.ebnbin.floatingcamera.util.BaseRuntimeException

/**
 * 应用 [Application] 类.
 */
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        singleton = this
    }

    companion object {
        private var singleton: AppApplication? = null

        val instance get() = singleton ?: throw BaseRuntimeException()
    }
}
