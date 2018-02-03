package com.ebnbin.floatingcamera

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.StringRes
import android.support.v4.util.ArrayMap
import android.view.WindowManager

//*********************************************************************************************************************
// 异常.

typealias BaseException = Exception
typealias BaseRuntimeException = RuntimeException

//*********************************************************************************************************************
// 单例.

val app by lazy { AppApplication.instance }

//*********************************************************************************************************************
// System services.

val windowManager by lazy { app.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

//*********************************************************************************************************************
// Resources.

val resources by lazy { app.resources!! }

private val stringCache by lazy { ArrayMap<@StringRes Int, String>() }

fun getString(@StringRes stringRes: Int) = stringCache.getOrPut(stringRes) { resources.getString(stringRes) }!!

val Int.dp get() = this * app.resources.displayMetrics.density
val Int.dpInt get() = dp.toInt()

//*********************************************************************************************************************
// TaskDescription.

@Suppress("DEPRECATION")
val taskDescription by lazy {
    val label = getString(R.string.app_name)

    val size = 48.dpInt
    val icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(icon)
    val drawable = resources.getDrawable(R.drawable.logo)
    val tintColor = resources.getColor(R.color.dark_icon)
    drawable.setTint(tintColor)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    val colorPrimary = resources.getColor(R.color.light_color_primary)

    ActivityManager.TaskDescription(label, icon, colorPrimary)
}
