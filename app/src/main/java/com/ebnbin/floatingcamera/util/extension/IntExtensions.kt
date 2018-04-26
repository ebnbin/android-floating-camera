package com.ebnbin.floatingcamera.util.extension

import com.ebnbin.floatingcamera.util.res

/**
 * Dp 转化 px. 例如 160dp = 320px 则 `160.dp = 320f`.
 */
val Int.dp get() = this * res.displayMetrics.density
/**
 * Dp 转化 px Int. 例如 160dp = 320px 则 `160.dpInt = 320`.
 */
val Int.dpInt get() = dp.toInt()

/**
 * 最大公约数. 如果 [other] 为 `0` 则返回 `0`.
 */
tailrec infix fun Int.gcd(other: Int): Int {
    if (other == 0) return 0

    return if (this % other == 0) other else other gcd this % other
}
