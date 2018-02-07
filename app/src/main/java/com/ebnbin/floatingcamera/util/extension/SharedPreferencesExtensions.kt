package com.ebnbin.floatingcamera.util.extension

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.ebnbin.floatingcamera.util.BaseRuntimeException

/**
 * 读 [SharedPreferences].
 */
@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
fun <T> SharedPreferences.get(key: String, defValue: T) = when (defValue) {
    is String -> getString(key, defValue)
    is Int -> getInt(key, defValue)
    is Long -> getLong(key, defValue)
    is Float -> getFloat(key, defValue)
    is Boolean -> getBoolean(key, defValue)
    else -> throw BaseRuntimeException()
} as T

/**
 * 写 [SharedPreferences]. 如果 [value] 为 `null` 则删除值.
 */
@SuppressLint("CommitPrefEdits")
fun <T> SharedPreferences.put(key: String, value: T?) = with(edit()) {
    when (value) {
        null -> remove(key)
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Float -> putFloat(key, value)
        is Boolean -> putBoolean(key, value)
        else -> throw BaseRuntimeException()
    }.apply()
}
