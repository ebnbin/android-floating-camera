package com.ebnbin.floatingcamera.fragment.preference

import android.support.v14.preference.SwitchPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.cameraHelper
import com.ebnbin.floatingcamera.defaultSharedPreferences
import com.ebnbin.floatingcamera.get

/**
 * 后置/前置摄像头偏好.
 */
class IsFrontPreference(devicePreferenceGroup: DevicePreferenceGroup) :
        SwitchPreference(devicePreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.is_front_title)
        setSummaryOff(R.string.is_front_summary_off)
        setSummaryOn(R.string.is_front_summary_on)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as Boolean

            devicePreferenceGroup.backPreferenceGroup?.isGroupVisible = !newValue
            devicePreferenceGroup.frontPreferenceGroup?.isGroupVisible = newValue

            true
        }
        isEnabled = cameraHelper.hasBothDevices

        devicePreferenceGroup.addPreferenceToGroup(this)

        if (!cameraHelper.hasBothDevices) {
            isChecked = cameraHelper.hasFrontDevice
//            callChangeListener(cameraHelper.hasFrontDevice)
        }
    }

    companion object {
        private const val KEY = "is_front"
        private const val DEF_VALUE = false

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
