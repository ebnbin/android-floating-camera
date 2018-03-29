package com.ebnbin.floatingcamera.widget

import android.content.Context
import android.util.AttributeSet

class PhotoCameraView : CameraView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onTap() {
        post { capture() }
    }

    override fun beforeOpenCamera() {
        super.beforeOpenCamera()

        initImageReader()
    }

    override fun afterOpenCamera() {
        super.afterOpenCamera()

        startPhotoPreview()
    }

    override fun beforeCloseCamera() {
        stopPhotoPreview()

        super.beforeCloseCamera()
    }

    override fun afterCloseCamera() {
        disposeImageReader()

        super.afterCloseCamera()
    }
}
