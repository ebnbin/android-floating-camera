package com.ebnbin.floatingcamera.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.hardware.camera2.CameraManager
import android.support.v7.preference.PreferenceManager
import android.view.WindowManager
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
 * 默认偏好.
 */
val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(app) }

val cameraManager by lazy { app.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
val windowManager by lazy { app.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

val cameraHelper get() = CameraHelper.instance
