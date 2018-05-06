package com.ebnbin.floatingcamera.util

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.view.Surface
import com.ebnbin.floatingcamera.dev.DevHelper

//*********************************************************************************************************************
// Display.

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
    DevHelper.event("display real size", mapOf("x" to outSize.x, "y" to outSize.y))
    WindowSize(outSize.x, outSize.y)
}

/**
 * 屏幕大小.
 */
val displaySize by lazy {
    val outSize = Point()
    display.getSize(outSize)
    DevHelper.event("display size", mapOf("x" to outSize.x, "y" to outSize.y))
    WindowSize(outSize.x, outSize.y)
}

/**
 * 屏幕旋转 270 度且水平方向有 NavigationBar 且 api >= 25 时, NavigationBar 在右侧.
 */
fun getNavigationBarXOffset(rotation: Int = displayRotation()): Int {
    val widthDiff = displayRealSize.width(rotation) - displaySize.width(rotation)
    return if (rotation == 3 && widthDiff > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) -widthDiff else 0
}

/**
 * Gets a color attribute by [attrId], or returns [Color.TRANSPARENT] if failed.
 */
@ColorInt
fun getColorAttr(context: Context, @AttrRes attrId: Int): Int {
    val attrs = intArrayOf(attrId)
    val ta = context.obtainStyledAttributes(attrs)

    val index = 0
    val defValue = Color.TRANSPARENT
    val result = ta.getColor(index, defValue)

    ta.recycle()

    return result
}
