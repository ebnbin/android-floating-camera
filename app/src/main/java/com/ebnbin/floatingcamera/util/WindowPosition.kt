package com.ebnbin.floatingcamera.util

/**
 * 窗口位置.
 */
class WindowPosition(x: Int, y: Int, rotation: Int = getDisplayRotation()) {
    val x0: Int
    val y0: Int

    val x90: Int
    val y90: Int

    val x180: Int
    val y180: Int

    val x270: Int
    val y270: Int

    init {
        when (rotation) {
            0 -> {
                x0 = x
                y0 = y

                x90 = y
                y90 = 100 - x

                x180 = 100 - x
                y180 = 100 - y

                x270 = 100 - y
                y270 = x
            }
            1 -> {
                x90 = x
                y90 = y

                x180 = y
                y180 = 100 - x

                x270 = 100 - x
                y270 = 100 - y

                x0 = 100 - y
                y0 = x
            }
            2 -> {
                x180 = x
                y180 = y

                x270 = y
                y270 = 100 - x

                x0 = 100 - x
                y0 = 100 - y

                x90 = 100 - y
                y90 = x
            }
            3 -> {
                x270 = x
                y270 = y

                x0 = y
                y0 = 100 - x

                x90 = 100 - x
                y90 = 100 - y

                x180 = 100 - y
                y180 = x
            }
            else -> throw BaseRuntimeException()
        }
    }

    fun x(rotation: Int = getDisplayRotation()) = when (rotation) {
        0 -> x0
        1 -> x90
        2 -> x180
        3 -> x270
        else -> throw BaseRuntimeException()
    }

    fun y(rotation: Int = getDisplayRotation()) = when (rotation) {
        0 -> y0
        1 -> y90
        2 -> y180
        3 -> y270
        else -> throw BaseRuntimeException()
    }
}
