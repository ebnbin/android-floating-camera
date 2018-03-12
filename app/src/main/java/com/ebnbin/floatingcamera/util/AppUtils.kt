package com.ebnbin.floatingcamera.util

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.util.ArrayMap
import android.view.Surface
import android.view.WindowManager
import com.ebnbin.floatingcamera.AppApplication
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.extension.dpInt

val app by lazy { AppApplication.instance }

//*********************************************************************************************************************

val packageName by lazy { app.packageName!! }

val packageUri by lazy { Uri.parse("package:$packageName")!! }

//*********************************************************************************************************************
// System services.

// TODO: Nullable 如果 Camera2 api 不支持.
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
fun getSharedPreferences(name: String = "${packageName}_preferences", mode: Int = Context.MODE_PRIVATE) =
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
fun displayRotation() = display.rotation

/**
 * 返回屏幕旋转方向是否为横向.
 */
fun isDisplayRotationLandscape(rotation: Int = displayRotation()) =
        rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270

/**
 * 屏幕大小.
 */
val displayRealSize by lazy {
    val outSize = Point()
    display.getRealSize(outSize)
    WindowSize(outSize.x, outSize.y)
}

/**
 * 屏幕大小.
 */
val displaySize by lazy {
    val outSize = Point()
    display.getSize(outSize)
    WindowSize(outSize.x, outSize.y)
}

/**
 * 屏幕旋转 270 度且水平方向有 NavigationBar 且 api >= 25 时, NavigationBar 在右侧.
 */
fun getNavigationBarXOffset(rotation: Int = displayRotation()): Int {
    val widthDiff = displayRealSize.width(rotation) - displaySize.width(rotation)
    return if (rotation == 3 && widthDiff > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) -widthDiff else 0
}

//*********************************************************************************************************************

val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(app) }
