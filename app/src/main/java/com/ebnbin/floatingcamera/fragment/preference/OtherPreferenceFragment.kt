package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.os.Bundle
import android.support.v7.preference.Preference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.PreferenceFragment
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.res

/**
 * 其他偏好界面.
 */
class OtherPreferenceFragment : PreferenceFragment<OtherPreferenceFragment.OtherRootPreferenceGroup>() {
    override fun createRootPreferenceGroup(context: Context) = OtherRootPreferenceGroup(context)

    /**
     *     PathPreference
     *     IsDarkThemePreference
     */
    class OtherRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
        /**
         * 存储路径.
         */
        private val pathPreference by lazy {
            Preference(context).apply {
                setTitle(R.string.path_title)
                summary = res.getString(R.string.path_summary, FileUtil.getPath())
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?) = arrayOf(
                pathPreference)
    }
}
