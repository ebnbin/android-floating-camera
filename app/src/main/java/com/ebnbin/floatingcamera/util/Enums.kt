package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.R

/**
 * 预览.
 */
enum class Preview(val entry: String) {
    CAPTURE(res.getString(R.string.preview_entry_capture)),
    FULL(res.getString(R.string.preview_entry_full)),
    SCREEN(res.getString(R.string.preview_entry_screen)),
    SQUARE(res.getString(R.string.preview_entry_square));

    val indexString = ordinal.toString()

    companion object {
        val entries = Array(values().size) { values()[it].entry }
    }
}
