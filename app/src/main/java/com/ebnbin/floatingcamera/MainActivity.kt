package com.ebnbin.floatingcamera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ebnbin.floatingcamera.event.IsDarkThemeEvent
import com.ebnbin.floatingcamera.fragment.home.HomeFragment
import com.ebnbin.floatingcamera.util.CameraHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.eventBus
import com.ebnbin.floatingcamera.util.taskDescription
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTaskDescription(taskDescription)

        initTheme()

        super.onCreate(savedInstanceState)

        eventBus.register(this)

        RotationHelper.register(this)

        if (!CameraHelper.detect()) {
            finish()
            return
        }

        if (savedInstanceState != null) return

        supportFragmentManager.beginTransaction().add(android.R.id.content, HomeFragment()).commit()
    }

    private fun initTheme() {
        setTheme(if (PreferenceHelper.isDarkTheme()) R.style.DarkTheme else R.style.LightTheme)
    }

    override fun onResume() {
        super.onResume()

        RotationHelper.enable(this)
    }

    override fun onPause() {
        RotationHelper.disable(this)

        super.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: IsDarkThemeEvent) {
        recreate()
    }

    override fun onDestroy() {
        RotationHelper.unregister(this)

        eventBus.unregister(this)

        super.onDestroy()
    }

    companion object {
        fun launch(context: Context = app) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
