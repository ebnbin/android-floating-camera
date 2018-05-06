package com.ebnbin.floatingcamera.util

import android.graphics.Point
import android.view.Surface

/**
 * 返回屏幕方向是否为横向.
 *
 * @param rotation 屏幕方向. [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180], [Surface.ROTATION_270]
 * 之一.
 */
fun isDisplayLandscape(rotation: Int = display.rotation) = when (rotation) {
    Surface.ROTATION_0, Surface.ROTATION_180 -> false
    Surface.ROTATION_90, Surface.ROTATION_270 -> true
    else -> throw BaseRuntimeException()
}

/**
 * 屏幕大小 (不包括导航栏).
 */
val displaySize by lazy {
    val rotation = display.rotation
    val outSize = Point()
    display.getSize(outSize)
    Size(outSize.x, outSize.y, rotation)
}

/**
 * 屏幕大小.
 */
val displayRealSize by lazy {
    val rotation = display.rotation
    val outSize = Point()
    display.getRealSize(outSize)
    Size(outSize.x, outSize.y, rotation)
}
