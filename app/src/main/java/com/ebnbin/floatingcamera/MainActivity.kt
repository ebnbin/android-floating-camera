package com.ebnbin.floatingcamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ebnbin.floatingcamera.fragment.home.HomeFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTaskDescription(taskDescription)
        setTheme(R.style.LightTheme)

        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) return

        supportFragmentManager.beginTransaction().add(android.R.id.content, HomeFragment()).commit()
    }
}
