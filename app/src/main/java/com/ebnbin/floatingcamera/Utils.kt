package com.ebnbin.floatingcamera

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.camera2.CameraManager
import android.os.Parcel
import android.support.annotation.StringRes
import android.support.v4.util.ArrayMap
import android.support.v7.preference.ListPreference
import android.view.WindowManager
import com.ebnbin.floatingcamera.util.CameraHelper

//*********************************************************************************************************************
// 异常.

typealias BaseException = Exception
typealias BaseRuntimeException = RuntimeException

//*********************************************************************************************************************
// 单例.

val app by lazy { AppApplication.instance }

val cameraHelper get() = CameraHelper.instance

//*********************************************************************************************************************
// System services.

val cameraManager by lazy { app.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
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

//*********************************************************************************************************************
// SharedPreferences.

@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
fun <T> SharedPreferences.get(key: String, defValue: T) = when (defValue) {
    is String -> getString(key, defValue)
    is Int -> getInt(key, defValue)
    is Long -> getLong(key, defValue)
    is Float -> getFloat(key, defValue)
    is Boolean -> getBoolean(key, defValue)
    else -> throw BaseRuntimeException()
} as T

@SuppressLint("CommitPrefEdits")
fun <T> SharedPreferences.put(key: String, value: T) = with(edit()) {
    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Float -> putFloat(key, value)
        is Boolean -> putBoolean(key, value)
        else -> throw RuntimeException()
    }.apply()
}

/**
 * 默认 [SharedPreferences].
 */
val defaultSharedPreferences = getSharedPreferences()

/**
 * 获取 [SharedPreferences].
 */
fun getSharedPreferences(name: String = "${app.packageName}_preferences", mode: Int = Context.MODE_PRIVATE) =
        app.getSharedPreferences(name, mode)!!

//*********************************************************************************************************************
// Math.

/**
 * 最大公约数. 如果 [other] 为 `0` 则返回 `0`.
 */
tailrec infix fun Int.gcd(other: Int): Int {
    if (other == 0) return 0

    return if (this % other == 0) other else other gcd this % other
}

//*********************************************************************************************************************
// Parcel.

fun Parcel.readBoolean(): Boolean {
    return readByte() != 0.toByte()
}

fun Parcel.writeBoolean(value: Boolean) {
    writeByte(if (value) 1 else 0)
}

//*********************************************************************************************************************
// Preference.

/**
 * 设置 [ListPreference.mEntries] 并以 index 字符串设置 [ListPreference.mEntryValues].
 */
fun ListPreference.setEntriesAndEntryValues(entries: Array<out CharSequence>) {
    this.entries = entries
    entryValues = Array(this.entries.size) { it.toString() }
}
