package com.ebnbin.floatingcamera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopCameraServiceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        CameraService.stop()
    }
}
