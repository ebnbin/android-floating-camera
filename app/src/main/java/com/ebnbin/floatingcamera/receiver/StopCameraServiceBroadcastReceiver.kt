package com.ebnbin.floatingcamera.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ebnbin.floatingcamera.service.CameraService

class StopCameraServiceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        CameraService.stop()
    }
}
