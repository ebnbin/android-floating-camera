package com.ebnbin.floatingcamera.util

import android.os.Environment
import java.io.File

/**
 * 文件工具类.
 */
object FileUtil {
    fun getPath(): File {
        val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), app.packageName)
        if (!path.exists()) path.mkdirs()
        return path
    }

    fun getFile(extension: String) = File(getPath(), "${System.currentTimeMillis()}$extension")
}
