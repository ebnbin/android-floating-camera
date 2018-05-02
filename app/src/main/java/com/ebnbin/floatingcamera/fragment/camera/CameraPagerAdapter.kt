package com.ebnbin.floatingcamera.fragment.camera

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.fragment.preference.CameraPreferenceFragment
import com.ebnbin.floatingcamera.fragment.preference.OtherPreferenceFragment
import com.ebnbin.floatingcamera.fragment.preference.WindowPreferenceFragment
import com.ebnbin.floatingcamera.util.res

class CameraPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int) = ITEMS[position].createFragment()

    override fun getCount() = ITEMS.size

    override fun getPageTitle(position: Int): String = res.getString(ITEMS[position].titleStringRes)

    companion object {
        private val ITEMS = arrayOf(
                Item({ WindowPreferenceFragment() }, R.string.window_title),
                Item({ CameraPreferenceFragment() }, R.string.camera_title),
                Item({ OtherPreferenceFragment() }, R.string.other_title))

        private class Item(val createFragment: () -> Fragment, @StringRes val titleStringRes: Int)
    }
}
