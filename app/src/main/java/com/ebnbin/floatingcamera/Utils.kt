package com.ebnbin.floatingcamera

import android.content.Context
import android.view.WindowManager

//*********************************************************************************************************************
// 异常.

typealias BaseException = Exception
typealias BaseRuntimeException = RuntimeException

//*********************************************************************************************************************
// 单例.

val app by lazy { AppApplication.instance }

//*********************************************************************************************************************
// System services.

val windowManager by lazy { app.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
