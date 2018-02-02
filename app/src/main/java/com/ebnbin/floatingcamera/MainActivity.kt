package com.ebnbin.floatingcamera

import android.app.Activity
import android.os.Bundle
import com.ebnbin.floatingcamera.widget.JCamera2VideoTextureView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(JCamera2VideoTextureView(this))
    }
}
