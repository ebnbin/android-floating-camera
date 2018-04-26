package com.ebnbin.floatingcamera.fragment.permission

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.fragment.permission.PermissionFragment.Callback
import com.ebnbin.floatingcamera.fragment.permission.PermissionFragment.Companion.request
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.app

/**
 * 权限界面.
 *
 * 没有 ui.
 *
 * 如果从未请求过权限, 会正常弹出系统权限对话框. 如果权限被拒绝过, 或被永久拒绝, 或为特殊权限, 会弹出对应的提示对话框.
 *
 * 需要权限的 [Activity] 或 [Fragment] 需要实现 [Callback], 调用 [request] 请求权限, 并在 [Callback.onPermissionsResult]
 * 中处理权限获取成功或失败后的逻辑.
 */
class PermissionFragment : Fragment() {
    private lateinit var callback: Callback

    private var requestCode = 0
    private var permissions = arrayOf<String>()
    private var hasSystemAlertWindowPermission = false
    private var runtimePermissions = arrayOf<String>()
    private var enableDenied = true

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        val parentFragment = parentFragment
        val activity = activity
        callback = when {
            parentFragment is Callback -> parentFragment
            activity is Callback -> activity
            else -> throw BaseRuntimeException()
        }

        val arguments = arguments
        if (arguments != null) initArguments(arguments)
    }

    private fun initArguments(arguments: Bundle) {
        requestCode = arguments.getInt(KEY_REQUEST_CODE)
        permissions = arguments.getStringArray(KEY_PERMISSIONS) ?: arrayOf()
        val runtimePermissionList = ArrayList<String>()
        runtimePermissionList.addAll(permissions)
        hasSystemAlertWindowPermission = runtimePermissionList.remove(Manifest.permission.SYSTEM_ALERT_WINDOW)
        runtimePermissions = runtimePermissionList.toTypedArray()
        enableDenied = arguments.getBoolean(KEY_ENABLE_DENIED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 只有第一次 onCreate 才请求权限.
        if (!postRequest) return

        postRequest = false
        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()) {
            onPermissionsResult(true)
        } else {
            requestSystemAlertWindowPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestSystemAlertWindowPermission() {
        val context = context ?: return

        if (!hasSystemAlertWindowPermission || Settings.canDrawOverlays(context)) {
            requestRuntimePermissions(false)
            return
        }

        showRequestPermissionsDialog(Permission.SYSTEM_ALERT_WINDOW)
    }

    /**
     * @param hasRequested 是否是第一次请求权限.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestRuntimePermissions(hasRequested: Boolean) {
        val context = context ?: return

        var denied = false
        var deniedForever = false
        for (runtimePermission in runtimePermissions) {
            if (ContextCompat.checkSelfPermission(context, runtimePermission) == PackageManager.PERMISSION_GRANTED)
                continue

            if (shouldShowRequestPermissionRationale(runtimePermission)) {
                denied = true
                continue
            }

            deniedForever = true
        }

        when {
            denied -> showRequestPermissionsDialog(Permission.RUNTIME_PERMISSIONS)
            deniedForever -> {
                if (hasRequested) {
                    showRequestPermissionsDialog(Permission.RUNTIME_PERMISSIONS_DENIED_FOREVER)
                } else {
                    // 没有请求过权限, 直接请求.
                    onRequestPermissions(Permission.RUNTIME_PERMISSIONS)
                }
            }
            else -> onPermissionsResult(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showRequestPermissionsDialog(permission: Permission) {
        PermissionDialogFragment.show(childFragmentManager, permission)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onRequestPermissions(permission: Permission) {
        when (permission) {
            Permission.SYSTEM_ALERT_WINDOW -> {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        .setData(Uri.parse("package:${app.packageName}"))
                startActivityForResult(intent, REQUEST_CODE_SYSTEM_ALERT_WINDOW)
            }
            PermissionFragment.Permission.RUNTIME_PERMISSIONS -> {
                requestPermissions(runtimePermissions, REQUEST_CODE_RUNTIME_PERMISSIONS)
            }
            PermissionFragment.Permission.RUNTIME_PERMISSIONS_DENIED_FOREVER -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:${app.packageName}"))
                startActivityForResult(intent, REQUEST_CODE_RUNTIME_PERMISSIONS_DENIED_FOREVER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SYSTEM_ALERT_WINDOW -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

                requestSystemAlertWindowPermission()
            }
            REQUEST_CODE_RUNTIME_PERMISSIONS_DENIED_FOREVER -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

                requestRuntimePermissions(true)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_RUNTIME_PERMISSIONS -> {
                // 系统权限对话框 bug. permissions 数量可能为 0.
                requestRuntimePermissions(permissions.isNotEmpty())
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun onPermissionsResult(granted: Boolean) {
        callback.onPermissionsResult(requestCode, granted)

        fragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    interface Callback {
        /**
         * 如果调用 [request] 时不允许拒绝权限, 则拒绝权限时不会回调这个方法.
         */
        fun onPermissionsResult(requestCode: Int, granted: Boolean)
    }

    enum class Permission {
        SYSTEM_ALERT_WINDOW,
        RUNTIME_PERMISSIONS,
        RUNTIME_PERMISSIONS_DENIED_FOREVER
    }

    @RequiresApi(Build.VERSION_CODES.M)
    class PermissionDialogFragment : AppCompatDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = context ?: return super.onCreateDialog(savedInstanceState)
            val activity = activity ?: return super.onCreateDialog(savedInstanceState)

            val permissionFragment = parentFragment as? PermissionFragment ?: throw BaseRuntimeException()

            val permission = arguments?.getSerializable(KEY_PERMISSION) as Permission? ?: throw BaseRuntimeException()

            @StringRes val messageStringRes = when (permission) {
                PermissionFragment.Permission.SYSTEM_ALERT_WINDOW -> {
                    R.string.permission_message_system_alert_window
                }
                PermissionFragment.Permission.RUNTIME_PERMISSIONS -> {
                    R.string.permission_message_runtime_permissions
                }
                PermissionFragment.Permission.RUNTIME_PERMISSIONS_DENIED_FOREVER -> {
                    R.string.permission_message_runtime_permissions_denied_forever
                }
            }
            @StringRes val negativeTextStringRes = if (permissionFragment.enableDenied)
                R.string.permission_deny else
                R.string.permission_finish
            val negativeListener = DialogInterface.OnClickListener { _, _ ->
                if (permissionFragment.enableDenied) {
                    permissionFragment.onPermissionsResult(false)
                } else {
                    activity.finish()
                }
            }

            return AlertDialog.Builder(context)
                    .setMessage(messageStringRes)
                    .setPositiveButton(R.string.permission_request) { _, _ ->
                        permissionFragment.onRequestPermissions(permission)
                    }
                    .setNegativeButton(negativeTextStringRes, negativeListener)
                    .create()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        companion object {
            private const val KEY_PERMISSION = "permission"

            fun show(fm: FragmentManager, permission: Permission) {
                val fragment = PermissionDialogFragment()
                val arguments = Bundle()
                arguments.putSerializable(KEY_PERMISSION, permission)
                fragment.arguments = arguments
                fragment.isCancelable = false

                fragment.show(fm, null)
            }
        }
    }

    companion object {
        private const val KEY_REQUEST_CODE = "request_code"
        private const val KEY_PERMISSIONS = "permissions"
        private const val KEY_ENABLE_DENIED = "enable_denied"

        private const val REQUEST_CODE_SYSTEM_ALERT_WINDOW = 0x1
        private const val REQUEST_CODE_RUNTIME_PERMISSIONS_DENIED_FOREVER = 0x2

        private const val REQUEST_CODE_RUNTIME_PERMISSIONS = 0x3

        /**
         * 是否需要请求权限. 每一次调用 [request] 后只需要在 [onCreate] 中执行一次权限请求.
         */
        private var postRequest = false

        /**
         * 请求权限.
         *
         * @param requestCode 每一次请求都有一个唯一的 id. 在 [Callback] 中验证.
         *
         * @param permissions 权限.
         *
         * @param enableDenied 是否允许拒绝权限. 如果为 `false` 则拒绝权限时退出应用, 否则拒绝权限时回调 [Callback].
         */
        fun request(fm: FragmentManager, requestCode: Int, vararg permissions: String, enableDenied: Boolean = true) {
            if (postRequest) return

            postRequest = true

            val fragment = PermissionFragment()
            val arguments = Bundle()
            arguments.putInt(KEY_REQUEST_CODE, requestCode)
            arguments.putStringArray(KEY_PERMISSIONS, permissions)
            arguments.putBoolean(KEY_ENABLE_DENIED, enableDenied)
            fragment.arguments = arguments

            fm.beginTransaction().add(fragment, null).commit()
        }
    }
}
