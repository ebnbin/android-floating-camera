package com.ebnbin.floatingcamera.util

import android.os.Environment
import java.io.File

/**
 * 文件工具类.
 */
object FileUtil {
    val externalPath by lazy {
        val result = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/$packageName")
        if (!result.exists()) result.mkdirs()
        result
    }
    val internalPath: File? by lazy { app.getExternalFilesDir(null) }

    // TODO: 发生变化时需要删除 "is_external".
    val isInternalAvailable by lazy { internalPath != null }
}
