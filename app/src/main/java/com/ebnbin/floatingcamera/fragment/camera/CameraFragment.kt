package com.ebnbin.floatingcamera.fragment.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebnbin.floatingcamera.service.CameraService
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.fragment.permission.PermissionFragment
import com.ebnbin.floatingcamera.util.LocalBroadcastHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.extension.dpInt
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.put
import com.ebnbin.floatingcamera.util.getColorAttr
import com.ebnbin.floatingcamera.util.sp
import kotlinx.android.synthetic.main.camera_fragment.cameraFab
import kotlinx.android.synthetic.main.camera_fragment.tabLayout
import kotlinx.android.synthetic.main.camera_fragment.viewPager

/**
 * 首页.
 */
class CameraFragment :
        Fragment(),
        ViewPager.OnPageChangeListener,
        PermissionFragment.Callback,
        LocalBroadcastHelper.Receiver {
    override fun onReceive(context: Context, intent: Intent, action: String) {
        when (action) {
            CameraService.ACTION_CAMERA_SERVICE_IS_RUNNING -> invalidateCameraServiceIsRunning()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalBroadcastHelper.register(this, CameraService.ACTION_CAMERA_SERVICE_IS_RUNNING)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        tabLayout.setupWithViewPager(viewPager)

        val adapter = CameraPagerAdapter(childFragmentManager)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = adapter.count
        viewPager.pageMargin = 16.dpInt
        viewPager.setPageMarginDrawable(ColorDrawable(getColorAttr(context, R.attr.colorPlaceholder)))

        cameraFab.setOnClickListener {
            cameraFab.isEnabled = false

            val recordAudioPermission = if (PreferenceHelper.isPhoto())
                arrayOf() else
                arrayOf(Manifest.permission.RECORD_AUDIO)
            PermissionFragment.request(childFragmentManager, REQUEST_CODE_PERMISSION,
                    Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.CAMERA, *recordAudioPermission,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (savedInstanceState != null) return

        viewPager.currentItem = sp.get(KEY_PAGE, DEF_VALUE_PAGE)
    }

    override fun onPermissionsResult(requestCode: Int, granted: Boolean) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                cameraFab.isEnabled = true

                if (!granted) return

                if (CameraService.isRunning)
                    CameraService.postStop() else
                    CameraService.start()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        invalidateCameraServiceIsRunning()
    }

    private fun invalidateCameraServiceIsRunning() {
        cameraFab.isEnabled = true
        cameraFab.setImageResource(if (CameraService.isRunning) R.drawable.stop else R.drawable.camera)
    }

    override fun onDestroyView() {
        viewPager.removeOnPageChangeListener(this)

        super.onDestroyView()
    }

    override fun onDestroy() {
        LocalBroadcastHelper.unregister(this)

        super.onDestroy()
    }

    override fun onPageScrollStateChanged(state: Int) = Unit

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

    override fun onPageSelected(position: Int) {
        sp.put(KEY_PAGE, position)
    }

    companion object {
        const val KEY_PAGE = "page"
        private const val DEF_VALUE_PAGE = 1

        private const val REQUEST_CODE_PERMISSION = 0x1
    }
}
