package com.ebnbin.floatingcamera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ebnbin.floatingcamera.fragment.home.HomeFragment
import com.ebnbin.floatingcamera.fragment.permission.PermissionFragment
import com.ebnbin.floatingcamera.fragment.preference.other.OtherRootPreferenceGroup
import com.ebnbin.floatingcamera.util.CameraHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.taskDescription

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
        PermissionFragment.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTaskDescription(taskDescription)

        initTheme()

        super.onCreate(savedInstanceState)

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)

        RotationHelper.register(this)

        if (savedInstanceState != null) return

        PermissionFragment.request(supportFragmentManager, REQUEST_CODE_PERMISSION, Manifest.permission.CAMERA)
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
            OtherRootPreferenceGroup.KEY_IS_DARK_THEME -> {
                recreate()
            }
        }
    }

    override fun onPermissionsResult(requestCode: Int, granted: Boolean) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                if (!granted) return

                if (!CameraHelper.detect()) {
                    finish()
                    return
                }

                supportFragmentManager.beginTransaction().add(android.R.id.content, HomeFragment()).commit()
            }
        }
    }

    override fun onPause() {
        RotationHelper.disable(this)

        super.onPause()
    }

    override fun onDestroy() {
        RotationHelper.unregister(this)

        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onDestroy()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 0x1

        fun start(context: Context = app) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
