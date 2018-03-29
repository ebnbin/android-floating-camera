package com.ebnbin.floatingcamera.widget

import android.content.Context
import android.util.AttributeSet

class VideoCameraView : CameraView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onTap() {
        toggleRecord()
    }

    override fun beforeOpenCamera() {
        super.beforeOpenCamera()

        initMediaRecorder()
    }

    override fun afterOpenCamera() {
        super.afterOpenCamera()

        startVideoPreview()
    }

    override fun beforeCloseCamera() {
        stopRecord(false)

        super.beforeCloseCamera()
    }

    override fun afterCloseCamera() {
        disposeMediaRecorder()

        super.afterCloseCamera()
    }
}
