package com.ebnbin.floatingcamera.util

import android.os.Environment
import java.io.File

/**
 * 文件工具类.
 */
object FileUtil {
    val path: File get() {
        val result = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), packageName)
        if (!result.exists()) result.mkdirs()
        return result
    }
}
