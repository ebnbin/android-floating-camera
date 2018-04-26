package com.ebnbin.floatingcamera.util

import android.graphics.Point
import android.os.Build
import android.support.v4.content.LocalBroadcastManager
import android.view.Surface
import android.view.WindowManager

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
