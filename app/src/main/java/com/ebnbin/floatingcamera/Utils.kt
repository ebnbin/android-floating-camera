package com.ebnbin.floatingcamera

//*********************************************************************************************************************
// 异常.

typealias BaseException = Exception
typealias BaseRuntimeException = RuntimeException

//*********************************************************************************************************************
// 单例.

val app by lazy { AppApplication.instance }
