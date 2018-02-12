package com.ebnbin.floatingcamera.util

/**
 * 窗口大小.
 */
class WindowSize(width: Int, height: Int, isLandscape: Boolean = isDisplayRotationLandscape()) {
    val portraitWidth: Int
    val portraitHeight: Int

    val landscapeWidth: Int
    val landscapeHeight: Int

    init {
        if (isLandscape) {
            portraitWidth = height
            portraitHeight = width

            landscapeWidth = width
            landscapeHeight = height
        } else {
            portraitWidth = width
            portraitHeight = height

            landscapeWidth = height
            landscapeHeight = width
        }
    }

    fun width(isLandscape: Boolean = isDisplayRotationLandscape()) = if (isLandscape) landscapeWidth else portraitWidth

    fun height(isLandscape: Boolean = isDisplayRotationLandscape()) =
            if (isLandscape) landscapeHeight else portraitHeight
}
