package com.ebnbin.floatingcamera.fragment.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebnbin.floatingcamera.CameraService
import com.ebnbin.floatingcamera.R
import kotlinx.android.synthetic.main.home_fragment.cameraFab

/**
 * 首页.
 */
class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraFab.setOnClickListener { CameraService.start() }
    }
}
