package com.ebnbin.floatingcamera.fragment.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.dev.DevHelper
import com.ebnbin.floatingcamera.fragment.album.AlbumFragment
import com.ebnbin.floatingcamera.fragment.camera.CameraFragment
import com.ebnbin.floatingcamera.fragment.more.MoreFragment
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.put
import com.ebnbin.floatingcamera.util.sp
import kotlinx.android.synthetic.main.main_fragment.bottomNavigationView

class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            val foundCameraFragment = childFragmentManager.findFragmentByTag(CameraFragment::class.java.name)
            val foundAlbumFragment = childFragmentManager.findFragmentByTag(AlbumFragment::class.java.name)
            val foundMoreFragment = childFragmentManager.findFragmentByTag(MoreFragment::class.java.name)
            when (it.itemId) {
                R.id.camera -> {
                    DevHelper.event("navigation", mapOf("item" to "camera"))
                    val transaction = childFragmentManager.beginTransaction()
                    if (foundCameraFragment != null) {
                        transaction.show(foundCameraFragment)
                    } else {
                        transaction.add(R.id.fragment_container, CameraFragment(), CameraFragment::class.java.name)
                    }
                    if (foundAlbumFragment != null) {
                        transaction.hide(foundAlbumFragment)
                    }
                    if (foundMoreFragment != null) {
                        transaction.hide(foundMoreFragment)
                    }
                    transaction.commit()
                    sp.put(KEY_NAVIGATION, 0)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.album -> {
                    DevHelper.event("navigation", mapOf("item" to "album"))
                    val transaction = childFragmentManager.beginTransaction()
                    if (foundAlbumFragment != null) {
                        transaction.show(foundAlbumFragment)
                        if (foundAlbumFragment is AlbumFragment) {
                            foundAlbumFragment.requestPermissions()
                        }
                    } else {
                        transaction.add(R.id.fragment_container, AlbumFragment(), AlbumFragment::class.java.name)
                    }
                    if (foundCameraFragment != null) {
                        transaction.hide(foundCameraFragment)
                    }
                    if (foundMoreFragment != null) {
                        transaction.hide(foundMoreFragment)
                    }
                    transaction.commit()
                    sp.put(KEY_NAVIGATION, 1)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.more -> {
                    DevHelper.event("navigation", mapOf("item" to "more"))
                    val transaction = childFragmentManager.beginTransaction()
                    if (foundMoreFragment != null) {
                        transaction.show(foundMoreFragment)
                    } else {
                        transaction.add(R.id.fragment_container, MoreFragment(), MoreFragment::class.java.name)
                    }
                    if (foundCameraFragment != null) {
                        transaction.hide(foundCameraFragment)
                    }
                    if (foundAlbumFragment != null) {
                        transaction.hide(foundAlbumFragment)
                    }
                    transaction.commit()
                    sp.put(KEY_NAVIGATION, 2)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        if (savedInstanceState != null) return

        bottomNavigationView.selectedItemId = when (sp.get(KEY_NAVIGATION, DEF_VALUE_NAVIGATION)) {
            0 -> R.id.camera
            1 -> R.id.album
            2 -> R.id.more
            else -> R.id.camera
        }
    }

    companion object {
        private const val KEY_NAVIGATION = "navigation"
        private const val DEF_VALUE_NAVIGATION = 0
    }
}
