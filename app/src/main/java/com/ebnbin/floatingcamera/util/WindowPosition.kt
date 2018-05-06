package com.ebnbin.floatingcamera.util

/**
 * 窗口位置.
 */
class WindowPosition(xPercent: Int, yPercent: Int, rotation: Int = display.rotation) {
    val xPercent0: Int
    val yPercent0: Int

    val xPercent90: Int
    val yPercent90: Int

    val xPercent180: Int
    val yPercent180: Int

    val xPercent270: Int
    val yPercent270: Int

    init {
        when (rotation) {
            0 -> {
                xPercent0 = xPercent
                yPercent0 = yPercent

                xPercent90 = yPercent
                yPercent90 = 100 - xPercent

                xPercent180 = 100 - xPercent
                yPercent180 = 100 - yPercent

                xPercent270 = 100 - yPercent
                yPercent270 = xPercent
            }
            1 -> {
                xPercent90 = xPercent
                yPercent90 = yPercent

                xPercent180 = yPercent
                yPercent180 = 100 - xPercent

                xPercent270 = 100 - xPercent
                yPercent270 = 100 - yPercent

                xPercent0 = 100 - yPercent
                yPercent0 = xPercent
            }
            2 -> {
                xPercent180 = xPercent
                yPercent180 = yPercent

                xPercent270 = yPercent
                yPercent270 = 100 - xPercent

                xPercent0 = 100 - xPercent
                yPercent0 = 100 - yPercent

                xPercent90 = 100 - yPercent
                yPercent90 = xPercent
            }
            3 -> {
                xPercent270 = xPercent
                yPercent270 = yPercent

                xPercent0 = yPercent
                yPercent0 = 100 - xPercent

                xPercent90 = 100 - xPercent
                yPercent90 = 100 - yPercent

                xPercent180 = 100 - yPercent
                yPercent180 = xPercent
            }
            else -> throw BaseRuntimeException()
        }
    }

    fun xPercent(rotation: Int = display.rotation) = when (rotation) {
        0 -> xPercent0
        1 -> xPercent90
        2 -> xPercent180
        3 -> xPercent270
        else -> throw BaseRuntimeException()
    }

    fun yPercent(rotation: Int = display.rotation) = when (rotation) {
        0 -> yPercent0
        1 -> yPercent90
        2 -> yPercent180
        3 -> yPercent270
        else -> throw BaseRuntimeException()
    }

    fun x(windowSize: Size, rotation: Int = display.rotation): Int {
        val navigationBarXOffset = getNavigationBarXOffset(rotation)

        val windowWidth = windowSize.width(rotation)

        val xRange = displayRealSize.width(rotation) - windowWidth
        val xPercent = xPercent(rotation)
        val xOffset = 0 + navigationBarXOffset

        val leftRange = windowWidth
        val leftPercent = xPercent + 100
        val leftOffset = -leftRange + navigationBarXOffset

        val rightRange = windowWidth
        val rightPercent = xPercent - 100
        val rightOffset = xRange + navigationBarXOffset

        return when (xPercent) {
            in -100..-1 -> calcPosition(leftRange, leftPercent, leftOffset)
            in 0..100 -> calcPosition(xRange, xPercent, xOffset)
            in 101..200 -> calcPosition(rightRange, rightPercent, rightOffset)
            else -> throw BaseRuntimeException()
        }
    }

    fun y(windowSize: Size, rotation: Int = display.rotation): Int {
        val windowHeight = windowSize.height(rotation)

        val yRange = displayRealSize.height(rotation) - windowHeight
        val yPercent = yPercent(rotation)
        val yOffset = 0

        val topRange = windowHeight
        val topPercent = yPercent + 100
        val topOffset = -topRange

        val bottomRange = windowHeight
        val bottomPercent = yPercent - 100
        val bottomOffset = yRange

        return when (yPercent) {
            in -100..-1 -> calcPosition(topRange, topPercent, topOffset)
            in 0..100 -> calcPosition(yRange, yPercent, yOffset)
            in 101..200 -> calcPosition(bottomRange, bottomPercent, bottomOffset)
            else -> throw BaseRuntimeException()
        }
    }

    companion object {
        private fun calcPosition(range: Int, percent: Int, offset: Int) =
                (range * (percent.toFloat() / 100f) + offset).toInt()
    }
}
