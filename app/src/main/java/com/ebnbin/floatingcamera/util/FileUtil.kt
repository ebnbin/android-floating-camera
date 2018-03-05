package com.ebnbin.floatingcamera.util

import android.os.Environment
import java.io.File

/**
 * 文件工具类.
 */
object FileUtil {
    fun getPath(): File {
        val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), packageName)
        if (!path.exists()) path.mkdirs()
        return path
    }

    fun getFile(isPhoto: Boolean) = File(getPath(), "${System.currentTimeMillis()}.${if (isPhoto) "jpg" else "mp4"}")
}
