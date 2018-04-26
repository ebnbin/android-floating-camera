package com.ebnbin.floatingcamera.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ebnbin.floatingcamera.fragment.preference.CameraPreferenceFragment
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.extension.dp
import com.ebnbin.floatingcamera.util.localBroadcastManager
import com.ebnbin.floatingcamera.util.sp

class InfoView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        View(context, attrs, defStyleAttr, defStyleRes), SharedPreferences.OnSharedPreferenceChangeListener {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.dp
        val isPhoto = PreferenceHelper.isPhoto()
        color = if (isPhoto) Color.BLUE else Color.GREEN
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter(CameraView.ACTION_VIDEO))

        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        sp.unregisterOnSharedPreferenceChangeListener(this)

        localBroadcastManager.unregisterReceiver(broadcastReceiver)

        super.onDetachedFromWindow()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            val isRecording = intent.getBooleanExtra(CameraView.EXTRA_IS_RECORDING, false)
            paint.color = if (isRecording) Color.RED else Color.GREEN
            invalidate()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            CameraPreferenceFragment.KEY_BACK_IS_PHOTO, CameraPreferenceFragment.KEY_FRONT_IS_PHOTO -> {
                val isPhoto = PreferenceHelper.isPhoto()
                paint.color = if (isPhoto) Color.BLUE else Color.GREEN
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
    }
}
