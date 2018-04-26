package com.ebnbin.floatingcamera

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.PreferenceHelper
import io.fabric.sdk.android.Fabric

/**
 * 应用 [Application] 类.
 */
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        singleton = this

        Fabric.with(this, Crashlytics())

        PreferenceHelper
    }

    companion object {
        private var singleton: AppApplication? = null

        val instance get() = singleton ?: throw BaseRuntimeException()
    }
}
