package com.ebnbin.floatingcamera.preference

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceManager
import com.ebnbin.floatingcamera.util.extension.readBoolean
import com.ebnbin.floatingcamera.util.extension.writeBoolean

/**
 * [android.support.v7.preference.PreferenceGroup].
 *
 * 用于管理 [Preference], 没有可显示的内容, 可以嵌套添加.
 *
 * [PreferenceCategory] 作为普通 [Preference] 使用, 不用于添加子 [Preference].
 */
open class PreferenceGroup(context: Context,
        /**
         * 保存所有子 [Preference], 用于恢复可见性.
         */
        private val preferences: Array<out Preference?>? = null,
        private val onFirstAttachedToHierarchy: ((PreferenceGroup) -> Unit)? = null) :
        android.support.v7.preference.PreferenceGroup(context, null) {
    init {
        isVisible = false
    }

    /**
     * 通过添加和移除的方式设置子 [Preference] 的可见性.
     */
    var isGroupVisible = DEF_IS_GROUP_VISIBLE
        set(value) {
            if (field == value) return
            field = value

            if (field) {
                addAll()
            } else {
                removeAll()
            }
        }

    private var isFirstAttachedToHierarchy = true

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        if (!isFirstAttachedToHierarchy) return

        isFirstAttachedToHierarchy = false

        addAll()

        onFirstAttachedToHierarchy?.invoke(this)
    }

    protected open fun preferences(): Array<out Preference?>? = preferences

    private fun addAll() {
        preferences()?.forEach {
            if (it != null) {
                super.addPreference(it)
            }
        }
    }

    //*****************************************************************************************************************
    // Instance state.

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) return super.onRestoreInstanceState(state)

        super.onRestoreInstanceState(state.superState)
        isGroupVisible = state.isGroupVisible
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) return superState

        return SavedState(superState, isGroupVisible)
    }

    private class SavedState : BaseSavedState {
        val isGroupVisible: Boolean

        constructor(source: Parcel?) : super(source) {
            isGroupVisible = source?.readBoolean() ?: DEF_IS_GROUP_VISIBLE
        }
        constructor(superState: Parcelable?, isGroupVisible: Boolean) : super(superState) {
            this.isGroupVisible = isGroupVisible
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)

            parcel.writeBoolean(isGroupVisible)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

            override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
        }
    }

    //*****************************************************************************************************************

    @Deprecated("使用 addPreferenceToGroup 代替.", ReplaceWith("addPreferenceToGroup"))
    override fun addPreference(preference: Preference?) = super.addPreference(preference)

    //*****************************************************************************************************************

    companion object {
        /**
         * [isGroupVisible] 默认值.
         */
        private const val DEF_IS_GROUP_VISIBLE = true
    }
}
