package com.ebnbin.floatingcamera.util

import android.view.Surface

/**
 * 带屏幕方向的位置.
 *
 * @param xPercent 水平方向百分比.
 *
 * @param yPercent 垂直方向百分比.
 *
 * @param rotation 当前屏幕方向.
 */
class Position(private val xPercent: Int, private val yPercent: Int, private val rotation: Int) {
    /**
     * 返回指定屏幕方向的水平方向百分比.
     *
     * @param rotation 屏幕方向.
     */
    fun xPercent(rotation: Int) = when (rotation) {
        Surface.ROTATION_0 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> xPercent
                Surface.ROTATION_90 -> 100 - yPercent
                Surface.ROTATION_180 -> 100 - xPercent
                Surface.ROTATION_270 -> yPercent
                else -> throw BaseRuntimeException()
            }
        }
        Surface.ROTATION_90 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> yPercent
                Surface.ROTATION_90 -> xPercent
                Surface.ROTATION_180 -> 100 - yPercent
                Surface.ROTATION_270 -> 100 - xPercent
                else -> throw BaseRuntimeException()
            }
        }
        Surface.ROTATION_180 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> 100 - xPercent
                Surface.ROTATION_90 -> yPercent
                Surface.ROTATION_180 -> xPercent
                Surface.ROTATION_270 -> 100 - yPercent
                else -> throw BaseRuntimeException()
            }
        }
        Surface.ROTATION_270 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> 100 - yPercent
                Surface.ROTATION_90 -> 100 - xPercent
                Surface.ROTATION_180 -> yPercent
                Surface.ROTATION_270 -> xPercent
                else -> throw BaseRuntimeException()
            }
        }
        else -> throw BaseRuntimeException()
    }

    /**
     * 返回指定屏幕方向的垂直方向百分比.
     *
     * @param rotation 屏幕方向.
     */
    fun yPercent(rotation: Int) = when (rotation) {
        Surface.ROTATION_0 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> yPercent
                Surface.ROTATION_90 -> xPercent
                Surface.ROTATION_180 -> 100 - yPercent
                Surface.ROTATION_270 -> 100 - xPercent
                else -> throw BaseRuntimeException()
            }
        }
        Surface.ROTATION_90 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> 100 - xPercent
                Surface.ROTATION_90 -> yPercent
                Surface.ROTATION_180 -> xPercent
                Surface.ROTATION_270 -> 100 - yPercent
                else -> throw BaseRuntimeException()
            }
        }
        Surface.ROTATION_180 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> 100 - yPercent
                Surface.ROTATION_90 -> 100 - xPercent
                Surface.ROTATION_180 -> yPercent
                Surface.ROTATION_270 -> xPercent
                else -> throw BaseRuntimeException()
            }
        }
        Surface.ROTATION_270 -> {
            when (this.rotation) {
                Surface.ROTATION_0 -> xPercent
                Surface.ROTATION_90 -> 100 - yPercent
                Surface.ROTATION_180 -> 100 - xPercent
                Surface.ROTATION_270 -> yPercent
                else -> throw BaseRuntimeException()
            }
        }
        else -> throw BaseRuntimeException()
    }

    /**
     * 根据大小计算水平方向位置.
     *
     * @param size 大小.
     *
     * @param rotation 当前屏幕方向.
     *
     * @param isRealSize [displaySize] 或 [displayRealSize].
     */
    @Suppress("UnnecessaryVariable")
    fun x(size: Size, rotation: Int, isRealSize: Boolean): Int {
        val navigationBarXOffset = getNavigationBarXOffset(rotation)
        val width = size.width(rotation)
        val displaySize = if (isRealSize) displayRealSize else displaySize

        val xSize = displaySize.width(rotation) - width
        val xPercent = xPercent(rotation)
        val xOffset = navigationBarXOffset

        val leftSize = width
        val leftPercent = xPercent + 100
        val leftOffset = -leftSize + navigationBarXOffset

        val rightSize = width
        val rightPercent = xPercent - 100
        val rightOffset = xSize + navigationBarXOffset

        return when (xPercent) {
            in 0..100 -> calcPosition(xSize, xPercent, xOffset)
            in -100..-1 -> calcPosition(leftSize, leftPercent, leftOffset)
            in 101..200 -> calcPosition(rightSize, rightPercent, rightOffset)
            else -> throw BaseRuntimeException()
        }
    }

    /**
     * 根据大小计算垂直方向位置.
     *
     * @param size 大小.
     *
     * @param rotation 当前屏幕方向.
     *
     * @param isRealSize [displaySize] 或 [displayRealSize].
     */
    @Suppress("UnnecessaryVariable")
    fun y(size: Size, rotation: Int, isRealSize: Boolean): Int {
        val height = size.height(rotation)
        val displaySize = if (isRealSize) displayRealSize else displaySize

        val ySize = displaySize.height(rotation) - height
        val yPercent = yPercent(rotation)
        val yOffset = 0

        val topSize = height
        val topPercent = yPercent + 100
        val topOffset = -topSize

        val bottomSize = height
        val bottomPercent = yPercent - 100
        val bottomOffset = ySize

        return when (yPercent) {
            in 0..100 -> calcPosition(ySize, yPercent, yOffset)
            in -100..-1 -> calcPosition(topSize, topPercent, topOffset)
            in 101..200 -> calcPosition(bottomSize, bottomPercent, bottomOffset)
            else -> throw BaseRuntimeException()
        }
    }

    companion object {
        /**
         * 当系统版本大等于 25 且屏幕方向为 270 度时, navigation bar 在特殊方向.
         *
         * @param rotation 当前屏幕方向.
         */
        private fun getNavigationBarXOffset(rotation: Int): Int {
            val navigationBarHeight = displayRealSize.height(false) - displaySize.height(false)
            return if (v25() && rotation == Surface.ROTATION_270) -navigationBarHeight else 0
        }

        /**
         * 计算位置.
         *
         * @param size 宽高.
         *
         * @param percent 百分比. 0 到 100.
         *
         * @param offset 偏移.
         */
        private fun calcPosition(size: Int, percent: Int, offset: Int) = (size * percent / 100f + offset).toInt()
    }
}
