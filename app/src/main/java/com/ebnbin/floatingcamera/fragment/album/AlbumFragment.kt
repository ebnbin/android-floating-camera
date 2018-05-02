package com.ebnbin.floatingcamera.fragment.album

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.fragment.permission.PermissionFragment

class AlbumFragment : Fragment(), PermissionFragment.Callback {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.album_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) return

        requestPermissions()
    }

    fun requestPermissions() {
        PermissionFragment.request(childFragmentManager, REQUEST_CODE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onPermissionsResult(requestCode: Int, granted: Boolean) {
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> {
                if (granted) {
                    if (childFragmentManager.findFragmentByTag(AlbumContentFragment::class.java.name) == null) {
                        childFragmentManager.beginTransaction().add(R.id.fragment_container, AlbumContentFragment(),
                                AlbumContentFragment::class.java.name).commit()
                    }
                } else {
                    Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_EXTERNAL_STORAGE = 0x1
    }
}
