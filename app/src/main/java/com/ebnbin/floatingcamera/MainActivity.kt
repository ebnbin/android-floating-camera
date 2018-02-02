package com.ebnbin.floatingcamera

import android.app.Activity
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = Button(this)
        button.setOnClickListener { CameraService.start() }

        setContentView(button)
    }
}
