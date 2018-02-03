package com.ebnbin.floatingcamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTaskDescription(taskDescription)
        setTheme(R.style.LightTheme)

        super.onCreate(savedInstanceState)

        val button = Button(this)
        button.setOnClickListener { CameraService.start() }

        setContentView(button)
    }
}
