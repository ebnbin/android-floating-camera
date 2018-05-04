package com.ebnbin.floatingcamera

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.ebnbin.floatingcamera.fragment.camera.CameraFragment
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.extension.get
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

        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlytics)

        if (sp.get(KEY_VERSION_CODE, 0) < BuildConfig.VERSION_CODE) {
            sp.put(KEY_VERSION_CODE, BuildConfig.VERSION_CODE)
            sp.put(CameraFragment.KEY_PAGE, null)
        }

        PreferenceHelper
    }

    companion object {
        private var singleton: AppApplication? = null

        val instance get() = singleton ?: throw BaseRuntimeException()

        private const val KEY_VERSION_CODE = "version_code"
    }
}
