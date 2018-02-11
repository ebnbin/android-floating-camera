package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceGroup
import android.support.v7.preference.PreferenceManager
import com.ebnbin.floatingcamera.util.extension.readBoolean
import com.ebnbin.floatingcamera.util.extension.writeBoolean

/**
 * 基础偏好组. 用于管理 [Preference], 没有可显示的内容, 可以嵌套添加.
 *
 * [PreferenceCategory] 作为普通 [Preference] 使用, 不用于添加子 [Preference].
 */
abstract class BasePreferenceGroup(context: Context) : PreferenceGroup(context, null) {
    init {
        isVisible = false
    }

    /**
     * 保存所有子 [Preference], 用于恢复可见性.
     */
    val preferences = ArrayList<Preference>()

    /**
     * 调用 [addPreference] 并添加到 [preferences].
     *
     * @return 是否添加到 [preferences].
     */
    fun addPreferenceToGroup(preference: Preference): Boolean {
        return if (super.addPreference(preference) && !preferences.contains(preference))
            preferences.add(preference) else
            false
    }

    /**
     * 通过添加和移除的方式设置子 [Preference] 的可见性.
     */
    var isGroupVisible = DEF_IS_GROUP_VISIBLE
        set(value) {
            if (field == value) return
            field = value

            if (field) {
                preferences.forEach { super.addPreference(it) }
            } else {
                removeAll()
            }
        }

    private var isFirstAttachedToHierarchy = true

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        if (isFirstAttachedToHierarchy) {
            isFirstAttachedToHierarchy = false

            onFirstAttachedToHierarchy(preferenceManager)
        }
    }

    protected open fun onFirstAttachedToHierarchy(preferenceManager: PreferenceManager?) {
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
