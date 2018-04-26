package com.ebnbin.floatingcamera

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.extension.put
import com.ebnbin.floatingcamera.util.sp
import io.fabric.sdk.android.Fabric

/**
 * 应用 [Application] 类.
 */
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        singleton = this

        Fabric.with(this, Crashlytics())

        sp.put(KEY_VERSION_CODE, BuildConfig.VERSION_CODE)

        PreferenceHelper
    }

    companion object {
        private var singleton: AppApplication? = null

        val instance get() = singleton ?: throw BaseRuntimeException()

        private const val KEY_VERSION_CODE = "version_code"
    }
}
