package com.ebnbin.floatingcamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ebnbin.floatingcamera.event.IsDarkThemeEvent
import com.ebnbin.floatingcamera.fragment.home.HomeFragment
import com.ebnbin.floatingcamera.util.CameraHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.taskDescription
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTaskDescription(taskDescription)

        initTheme()

        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

        if (!CameraHelper.detect()) {
            finish()
            return
        }

        if (savedInstanceState != null) return

        supportFragmentManager.beginTransaction().add(android.R.id.content, HomeFragment()).commit()
    }

    private fun initTheme() {
        setTheme(if (PreferenceHelper.isDarkTheme) R.style.DarkTheme else R.style.LightTheme)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: IsDarkThemeEvent) {
        recreate()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)

        super.onDestroy()
    }
}
