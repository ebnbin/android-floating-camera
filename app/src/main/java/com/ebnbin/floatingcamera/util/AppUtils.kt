package com.ebnbin.floatingcamera.util

import android.content.Context
import android.graphics.Color
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt

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
