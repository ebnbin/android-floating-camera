package com.ebnbin.floatingcamera.fragment.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebnbin.floatingcamera.CameraService
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.put
import kotlinx.android.synthetic.main.home_fragment.cameraFab
import kotlinx.android.synthetic.main.home_fragment.tabLayout
import kotlinx.android.synthetic.main.home_fragment.viewPager

/**
 * 首页.
 */
class HomeFragment : Fragment(), ViewPager.OnPageChangeListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout.setupWithViewPager(viewPager)

        val adapter = HomePagerAdapter(childFragmentManager)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = adapter.count

        cameraFab.setOnClickListener { CameraService.start() }

        if (savedInstanceState != null) return

        viewPager.currentItem = defaultSharedPreferences.get(KEY_PAGE, DEF_VALUE_PAGE)
    }

    override fun onDestroyView() {
        viewPager.removeOnPageChangeListener(this)

        super.onDestroyView()
    }

    override fun onPageScrollStateChanged(state: Int) = Unit

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

    override fun onPageSelected(position: Int) {
        defaultSharedPreferences.put(KEY_PAGE, position)
    }

    companion object {
        private const val KEY_PAGE = "page"
        private const val DEF_VALUE_PAGE = 1
    }
}
