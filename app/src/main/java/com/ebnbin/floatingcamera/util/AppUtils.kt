package com.ebnbin.floatingcamera.util

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.hardware.camera2.CameraManager
import android.support.annotation.StringRes
import android.support.v4.util.ArrayMap
import android.view.Surface
import android.view.WindowManager
import com.ebnbin.floatingcamera.AppApplication
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.extension.dpInt

val app by lazy { AppApplication.instance }

//*********************************************************************************************************************
// System services.

val cameraManager by lazy { app.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
val windowManager by lazy { app.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

//*********************************************************************************************************************
// Resources.

val resources by lazy { app.resources!! }

private val stringCache by lazy { ArrayMap<@StringRes Int, String>() }

fun getString(@StringRes stringRes: Int) = stringCache.getOrPut(stringRes) { resources.getString(stringRes) }!!

//*********************************************************************************************************************
// TaskDescription.

@Suppress("DEPRECATION")
val taskDescription by lazy {
    val label = getString(R.string.app_name)

    val size = 48.dpInt
    val icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(icon)
    val drawable = resources.getDrawable(R.drawable.logo)
    val tintColor = resources.getColor(R.color.dark_icon)
    drawable.setTint(tintColor)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    val colorPrimary = resources.getColor(R.color.light_color_primary)

    ActivityManager.TaskDescription(label, icon, colorPrimary)
}

//*********************************************************************************************************************
// SharedPreferences.

/**
 * 默认 [SharedPreferences].
 */
val defaultSharedPreferences by lazy { getSharedPreferences() }

/**
 * 获取 [SharedPreferences].
 */
fun getSharedPreferences(name: String = "${app.packageName}_preferences", mode: Int = Context.MODE_PRIVATE) =
        app.getSharedPreferences(name, mode)!!

//*********************************************************************************************************************
// Display.

/**
 * [WindowManager.getDefaultDisplay].
 */
private val display by lazy { windowManager.defaultDisplay!! }

/**
 * 返回屏幕旋转方向.
 */
private fun getDisplayRotation() = display.rotation

/**
 * 返回屏幕旋转方向是否为横向.
 */
private fun isDisplayRotationLandscape(): Boolean {
    val rotation = getDisplayRotation()
    return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
}

/**
 * 返回屏幕宽高.
 */
private fun getDisplaySize(): Pair<Int, Int> {
    val outSize = Point()
    display.getRealSize(outSize)
    return Pair(outSize.x, outSize.y)
}

/**
 * 屏幕旋转方向为横向时的宽高.
 */
private val displayLandscapeSize by lazy {
    val isDisplayRotationLandscape = isDisplayRotationLandscape()
    val displaySize = getDisplaySize()
    Pair(if (isDisplayRotationLandscape) displaySize.first else displaySize.second,
            if (isDisplayRotationLandscape) displaySize.second else displaySize.first)
}

/**
 * 屏幕旋转方向为横向时的宽.
 */
val displayLandscapeWidth by lazy { displayLandscapeSize.first }
/**
 * 屏幕旋转方向为横向时的高.
 */
val displayLandscapeHeight by lazy { displayLandscapeSize.second }