package com.ebnbin.floatingcamera.util.extension

import android.os.Parcel

/**
 * 根据 [Byte] 读 [Boolean].
 */
fun Parcel.readBoolean(): Boolean {
    return readByte() != 0.toByte()
}

/**
 * 根据 [Byte] 写 [Boolean].
 */
fun Parcel.writeBoolean(value: Boolean) {
    writeByte(if (value) 1 else 0)
}
