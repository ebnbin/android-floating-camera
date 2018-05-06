package com.ebnbin.floatingcamera.util

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
