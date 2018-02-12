package com.ebnbin.floatingcamera.util

import android.content.Context
import android.support.v4.util.ArrayMap
import android.view.OrientationEventListener
import com.ebnbin.floatingcamera.event.RotationEvent
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.put

/**
 * 屏幕旋转方向帮助类.
 *
 * 用于检测屏幕旋转方向. 可以检测屏幕旋转 180 度. 当屏幕旋转方向变化时会写入偏好, 并发送 [RotationEvent].
 *
 * 如果没有任何对象被注册, 是不会检测屏幕旋转方向的.
 *
 * 如果没有任何对象被注册, 注册第一个对象时会立刻检测一次.
 */
object RotationHelper {
    private var rotation: Int = 0

    private val orientationEventListeners = ArrayMap<Context, OrientationEventListener>()

    /**
     * 注册.
     */
    fun register(context: Context): OrientationEventListener {
        if (orientationEventListeners.isEmpty) {
            val sharedPreferencesRotation = defaultSharedPreferences.get(KEY_ROTATION, DEF_VALUE_ROTATION)
            rotation = getDisplayRotation()
            if (sharedPreferencesRotation != rotation) {
                onRotationChanged(rotation)
            }
        }

        var orientationEventListener = orientationEventListeners[context]
        if (orientationEventListener != null) return orientationEventListener

        orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = getDisplayRotation()
                if (this@RotationHelper.rotation == rotation) return

                this@RotationHelper.rotation = rotation
                onRotationChanged(this@RotationHelper.rotation)
            }
        }
        orientationEventListeners[context] = orientationEventListener
        return orientationEventListener
    }

    /**
     * 注册并启用检测.
     */
    fun registerAndEnable(context: Context): OrientationEventListener {
        val orientationEventListener = register(context)
        orientationEventListener.enable()
        return orientationEventListener
    }

    /**
     * 启用检测.
     */
    fun enable(context: Context): Boolean {
        orientationEventListeners.getOrElse(context) {
            return false
        }.enable()
        return true
    }

    /**
     * 禁用检测.
     */
    fun disable(context: Context): Boolean {
        orientationEventListeners.getOrElse(context) {
            return false
        }.disable()
        return true
    }

    /**
     * 禁用检测并反注册.
     */
    fun unregister(context: Context): Boolean {
        val orientationEventListener = orientationEventListeners.remove(context) ?: return false
        orientationEventListener.disable()
        return true
    }

    private fun onRotationChanged(rotation: Int) {
        defaultSharedPreferences.put(KEY_ROTATION, rotation)

        eventBus.post(RotationEvent(rotation))

        listeners.forEach { it.onRotationChanged(rotation) }
    }

    val listeners = ArrayList<Listener>()

    interface Listener {
        fun onRotationChanged(rotation: Int)
    }

    private const val KEY_ROTATION = "rotation"
    private const val DEF_VALUE_ROTATION = 0
}
