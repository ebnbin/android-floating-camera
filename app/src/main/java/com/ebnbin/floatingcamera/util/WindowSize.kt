package com.ebnbin.floatingcamera.util

/**
 * 窗口大小.
 */
class WindowSize(width: Int, height: Int, isLandscape: Boolean = isDisplayRotationLandscape()) {
    val portraitWidth: Int
    val portraitHeight: Int

    val landscapeWidth: Int
    val landscapeHeight: Int

    val width0: Int
    val height0: Int

    val width90: Int
    val height90: Int

    val width180: Int
    val height180: Int

    val width270: Int
    val height270: Int

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

        width0 = portraitWidth
        height0 = portraitHeight

        width90 = landscapeWidth
        height90 = landscapeHeight

        width180 = portraitWidth
        height180 = portraitHeight

        width270 = landscapeWidth
        height270 = landscapeHeight
    }

    constructor(width: Int, height: Int, rotation: Int) : this(width, height, when (rotation) {
        0, 2 -> false
        1, 3 -> true
        else -> throw BaseRuntimeException()
    })

    fun width(isLandscape: Boolean = isDisplayRotationLandscape()) = if (isLandscape) landscapeWidth else portraitWidth

    fun width(rotation: Int) = when (rotation) {
        0, 2 -> portraitWidth
        1, 3 -> landscapeWidth
        else -> throw BaseRuntimeException()
    }

    fun height(isLandscape: Boolean = isDisplayRotationLandscape()) =
            if (isLandscape) landscapeHeight else portraitHeight

    fun height(rotation: Int) = when (rotation) {
        0, 2 -> portraitHeight
        1, 3 -> landscapeHeight
        else -> throw BaseRuntimeException()
    }
}
