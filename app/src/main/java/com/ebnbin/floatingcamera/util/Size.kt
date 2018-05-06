package com.ebnbin.floatingcamera.util

import android.view.Surface

/**
 * 带屏幕方向的大小.
 *
 * @param width 宽.
 *
 * @param height 高.
 *
 * @param isLandscape 当前屏幕方向是否为横向.
 */
class Size(private val width: Int, private val height: Int, private val isLandscape: Boolean) {
    /**
     * @param width 宽.
     *
     * @param height 高.
     *
     * @param rotation 当前屏幕方向. [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180],
     * [Surface.ROTATION_270] 之一.
     */
    constructor(width: Int, height: Int, rotation: Int) : this(width, height, isDisplayLandscape(rotation))

    /**
     * 返回指定屏幕方向的宽.
     *
     * @param isLandscape 屏幕方向是否为横向.
     */
    fun width(isLandscape: Boolean) = if (isLandscape) {
        if (this.isLandscape) width else height
    } else {
        if (this.isLandscape) height else width
    }

    /**
     * 返回指定屏幕方向的宽.
     *
     * @param rotation 屏幕方向. [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180],
     * [Surface.ROTATION_270] 之一.
     */
    fun width(rotation: Int) = width(isDisplayLandscape(rotation))

    /**
     * 返回指定屏幕方向的高.
     *
     * @param isLandscape 屏幕方向是否为横向.
     */
    fun height(isLandscape: Boolean) = if (isLandscape) {
        if (this.isLandscape) height else width
    } else {
        if (this.isLandscape) width else height
    }

    /**
     * 返回指定屏幕方向的高.
     *
     * @param rotation 屏幕方向. [Surface.ROTATION_0], [Surface.ROTATION_90], [Surface.ROTATION_180],
     * [Surface.ROTATION_270] 之一.
     */
    fun height(rotation: Int) = height(isDisplayLandscape(rotation))
}
