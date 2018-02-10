package com.ebnbin.floatingcamera.fragment.preference

open class PreferenceGroup(params: Params) : BasePreferenceGroup(params.preferenceGroup) {
    init {
        if (params.preferenceGroup is BasePreferenceGroup) {
            params.preferenceGroup.addPreferenceToGroup(this)
        } else {
            params.preferenceGroup.addPreference(this)
        }
    }

    companion object {
        open class Params(
                val preferenceGroup: android.support.v7.preference.PreferenceGroup)
    }
}
