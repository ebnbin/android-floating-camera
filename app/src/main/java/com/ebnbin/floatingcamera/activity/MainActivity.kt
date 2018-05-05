package com.ebnbin.floatingcamera.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.dev.DevHelper
import com.ebnbin.floatingcamera.fragment.main.MainFragment
import com.ebnbin.floatingcamera.fragment.more.MorePreferenceFragment
import com.ebnbin.floatingcamera.util.CameraException
import com.ebnbin.floatingcamera.util.CameraHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.extension.dpInt
import com.ebnbin.floatingcamera.util.res
import com.ebnbin.floatingcamera.util.sp

class MainActivity :
        AppCompatActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        initTheme()

        super.onCreate(savedInstanceState)

        DevHelper.event("main activity on create")

        setTaskDescription(taskDescription)

        sp.registerOnSharedPreferenceChangeListener(this)

        RotationHelper.register(this)

        if (savedInstanceState != null) return

        try {
            CameraHelper
        } catch (e: CameraException) {
            Toast.makeText(this, R.string.camera_exception, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportFragmentManager.beginTransaction().add(android.R.id.content, MainFragment()).commit()
    }

    @Suppress("DEPRECATION")
    private val taskDescription by lazy {
        val label = res.getString(R.string.app_name)

        val size = 48.dpInt
        val icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(icon)
        val drawable = res.getDrawable(R.drawable.logo)
        val tintColor = res.getColor(R.color.dark_icon)
        drawable.setTint(tintColor)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val colorPrimary = res.getColor(R.color.light_color_primary)

        ActivityManager.TaskDescription(label, icon, colorPrimary)
    }

    private fun initTheme() {
        setTheme(if (PreferenceHelper.isDarkTheme()) R.style.DarkTheme else R.style.LightTheme)
    }

    override fun onResume() {
        super.onResume()

        RotationHelper.enable(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MorePreferenceFragment.KEY_IS_DARK_THEME -> {
                recreate()
            }
        }
    }

    override fun onPause() {
        RotationHelper.disable(this)

        super.onPause()
    }

    override fun onDestroy() {
        RotationHelper.unregister(this)

        sp.unregisterOnSharedPreferenceChangeListener(this)

        super.onDestroy()
    }

    companion object {
        fun start(context: Context = app) {
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
