package com.ebnbin.floatingcamera.util

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.hardware.camera2.CameraManager
import android.support.v7.preference.PreferenceManager
import android.view.Display
import android.view.WindowManager
import com.crashlytics.android.answers.Answers
import com.ebnbin.floatingcamera.AppApplication

/**
 * Application 单例.
 */
val app by lazy { AppApplication.instance }

/**
 * Application 资源.
 */
val res: Resources by lazy { app.resources }

/**
 * [WindowManager].
 */
val windowManager: WindowManager by lazy {
    if (v23()) {
        app.getSystemService(WindowManager::class.java)
    } else {
        app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
}
/**
 * [NotificationManager].
 */
val notificationManager: NotificationManager by lazy {
    if (v23()) {
        app.getSystemService(NotificationManager::class.java)
    } else {
        app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
/**
 * [CameraManager].
 */
val cameraManager: CameraManager by lazy {
    if (v23()) {
        app.getSystemService(CameraManager::class.java)
    } else {
        app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
}

/**
 * [WindowManager.getDefaultDisplay].
 */
val display: Display by lazy { windowManager.defaultDisplay }

/**
 * 默认偏好.
 */
val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(app) }

/**
 * [Answers].
 */
val answers: Answers by lazy { Answers.getInstance() }
