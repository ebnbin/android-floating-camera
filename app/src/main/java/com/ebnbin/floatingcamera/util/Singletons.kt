package com.ebnbin.floatingcamera.util

import org.greenrobot.eventbus.EventBus

val cameraHelper get() = CameraHelper.instance

val eventBus by lazy { EventBus.getDefault()!! }
