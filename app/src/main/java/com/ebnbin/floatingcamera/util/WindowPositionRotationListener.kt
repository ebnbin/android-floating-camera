package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.fragment.preference.window.WindowRootPreferenceGroup

/**
 * 用于更新窗口位置的 [RotationHelper.Listener].
 */
object WindowPositionRotationListener : RotationHelper.Listener {
    override fun onRotationChanged(oldRotation: Int, newRotation: Int) {
        val windowPosition = WindowPosition(WindowRootPreferenceGroup.windowX, WindowRootPreferenceGroup.windowY,
                oldRotation)
        WindowRootPreferenceGroup.putWindowPosition(windowPosition.x(newRotation), windowPosition.y(newRotation))
    }
}
