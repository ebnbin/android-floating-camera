package com.ebnbin.floatingcamera.util

import com.ebnbin.floatingcamera.R

/**
 * 预览.
 */
enum class Preview(val entry: String) {
    CAPTURE(getString(R.string.preview_entry_capture)),
    FULL(getString(R.string.preview_entry_full)),
    SCREEN(getString(R.string.preview_entry_screen));

    val indexString = ordinal.toString()

    companion object {
        val entries = Array(values().size) { values()[it].entry }
    }
}
