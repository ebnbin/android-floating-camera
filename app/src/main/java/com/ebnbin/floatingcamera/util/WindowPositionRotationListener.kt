package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.fragment.preference.WindowPreferenceFragment

/**
 * 用于更新窗口位置的 [RotationHelper.Listener].
 */
object WindowPositionRotationListener : RotationHelper.Listener {
    override fun onRotationChanged(oldRotation: Int, newRotation: Int) {
        val windowPosition = WindowPosition(WindowPreferenceFragment.windowX, WindowPreferenceFragment.windowY,
                oldRotation)
        WindowPreferenceFragment.putWindowPosition(windowPosition.xPercent(newRotation),
                windowPosition.yPercent(newRotation))
    }
}
