package com.ebnbin.floatingcamera.util

import android.os.Build

/**
 * 系统版本大等于 23 ([Build.VERSION_CODES.M]).
 */
fun v23() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/**
 * 系统版本大等于 25 ([Build.VERSION_CODES.N_MR1]).
 */
fun v25() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

/**
 * 系统版本大等于 26 ([Build.VERSION_CODES.O]).
 */
fun v26() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
