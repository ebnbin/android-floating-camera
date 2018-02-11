package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager

open class PreferenceGroup(context: Context, private val params: Params? = null) : BasePreferenceGroup(context) {
    override fun onFirstAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onFirstAttachedToHierarchy(preferenceManager)

        params?.preferences?.forEach {
            if (it != null) {
                addPreferenceToGroup(it)
            }
        }
        params?.init?.invoke(this)
    }

    companion object {
        open class Params(
                val preferences: Array<Preference?>? = null,
                val init: ((PreferenceGroup) -> Unit)? = null)
    }
}
