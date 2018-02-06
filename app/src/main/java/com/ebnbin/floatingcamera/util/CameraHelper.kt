package com.ebnbin.floatingcamera.util

import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import com.ebnbin.floatingcamera.BaseRuntimeException
import com.ebnbin.floatingcamera.cameraManager
import kotlin.math.min

/**
 * 相机帮助类.
 *
 * @throws CameraException
 */
@Suppress("DEPRECATION")
class CameraHelper private constructor() {
    /**
     * Camera2 id.
     */
    private val ids2: Array<String> = try {
        cameraManager.cameraIdList
    } catch (e: CameraAccessException) {
        throw CameraException("Camera2 id 获取失败.", e)
    }

    /**
     * Camera2 id 数量.
     */
    private val idSize2 = ids2.size
    init {
        if (idSize2 <= 0) throw CameraException("Camera2 id 数量为 0.")
    }

    /**
     * Camera1 id 数量.
     */
    private val idSize1 = Camera.getNumberOfCameras()
    init {
        if (idSize1 <= 0) throw CameraException("Camera1 id 数量为 0.")
    }

    /**
     * Id 数量. [idSize2] 和 [idSize1] 取小值.
     */
    private val idSize = min(idSize2, idSize1)

    /**
     * 是否拥有后置摄像头.
     */
    val hasBackDevice: Boolean
    /**
     * 是否拥有前置摄像头.
     */
    val hasFrontDevice: Boolean
    /**
     * 是否拥有后置和前置摄像头.
     */
    val hasBothDevices: Boolean

    /**
     * 第一个后置摄像头. 用于 [backDevice].
     */
    private var firstBackDevice: Device? = null
    /**
     * 第一个前置摄像头. 用于 [frontDevice].
     */
    private var firstFrontDevice: Device? = null

    /**
     * 必须先判断 [hasBackDevice] 为 `true`.
     */
    val backDevice get() = firstBackDevice ?: throw BaseRuntimeException()
    /**
     * 必须先判断 [hasFrontDevice] 为 `true`.
     */
    val frontDevice get() = firstFrontDevice ?: throw BaseRuntimeException()

    init {
        var hasBackDevice = false
        var hasFrontDevice = false

        for (index in 0 until idSize) {
//            // 如果两个摄像头都检测到了, 就跳出循环.
//            if (hasBackDevice && hasFrontDevice) break
//
            var camera1: Camera? = null
            try {
                try {
                    camera1 = Camera.open(index)
                } catch (e: RuntimeException) {
                    throw CameraException("Camera1 打开失败.", e)
                }

                val device = Device(ids2[index], index, camera1, hasBackDevice, hasFrontDevice)
                if (device.isFront) {
                    if (hasFrontDevice) throw BaseRuntimeException()

                    hasFrontDevice = true
                    firstFrontDevice = device
                } else {
                    if (hasBackDevice) throw BaseRuntimeException()

                    hasBackDevice = true
                    firstBackDevice = device
                }
            } catch (e: CameraException) {
                e.printStackTrace()
            } finally {
                camera1?.release()
            }
        }

        this.hasBackDevice = hasBackDevice
        this.hasFrontDevice = hasFrontDevice
        hasBothDevices = this.hasBackDevice && this.hasFrontDevice
    }

    /**
     * 摄像头.
     *
     * @param id2 Camera2 id.
     *
     * @param id1 Camera1 id.
     *
     * @param camera1 Camera1 摄像头. 已打开, 不需要释放. 用于检测 Camera1 参数.
     *
     * @param hasBackDevice 是否已检测到后置摄像头. 如果为 `true` 且当前摄像头也是后置摄像头则抛出 [CameraException].
     *
     * @param hasFrontDevice 是否已检测到前置摄像头. 如果为 `true` 且当前摄像头也是前置摄像头则抛出 [CameraException].
     */
    class Device(private val id2: String, private val id1: Int, camera1: Camera, hasBackDevice: Boolean,
            hasFrontDevice: Boolean) {
        /**
         * Camera2 [CameraCharacteristics].
         */
        private val cameraCharacteristics2 = try {
            cameraManager.getCameraCharacteristics(id2)
                    ?: throw CameraException("Camera2 CameraCharacteristics 获取失败.")
        } catch (e: Exception) {
            when (e) {
                is CameraAccessException -> throw CameraException("Camera2 CameraCharacteristics 获取失败.", e)
                else -> throw e
            }
        }

        /**
         * Camera1 [Camera.CameraInfo].
         */
        private val cameraInfo1 = Camera.CameraInfo()
        init {
            try {
                Camera.getCameraInfo(id1, cameraInfo1)
            } catch (e: RuntimeException) {
                throw CameraException("Camera1 CameraInfo 获取失败.", e)
            }
        }

        /**
         * Camera2 朝向.
         */
        private val lensFacing2 = cameraCharacteristics2.get(CameraCharacteristics.LENS_FACING)
                ?: throw CameraException("Camera2 朝向获取失败.")

        /**
         * Camera1 朝向.
         */
        private val facing1 = cameraInfo1.facing

        init {
            when (lensFacing2) {
                CameraMetadata.LENS_FACING_FRONT -> {
                    if (hasFrontDevice) throw CameraException("超过 1 个前置摄像头.")

                    if (facing1 != Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        throw CameraException("Camera2 和 Camera1 朝向参数异常.")
                    }
                }
                CameraMetadata.LENS_FACING_BACK -> {
                    if (hasBackDevice) throw CameraException("超过 1 个后置摄像头.")

                    if (facing1 != Camera.CameraInfo.CAMERA_FACING_BACK) {
                        throw CameraException("Camera2 和 Camera1 朝向参数异常.")
                    }
                }
                CameraMetadata.LENS_FACING_EXTERNAL -> throw CameraException("不支持外置摄像头.")
                else -> throw BaseRuntimeException()
            }
        }

        /**
         * 是否为前置摄像头.
         */
        val isFront = lensFacing2 == CameraMetadata.LENS_FACING_FRONT
    }

    companion object {
        private var singleton: CameraHelper? = null

        val instance get() = singleton ?: throw BaseRuntimeException()

        /**
         * 检测相机. 返回摄像头是否可用. 需要在初始化时调用一次.
         */
        fun detect(): Boolean {
            singleton = try {
                CameraHelper()
            } catch (e: CameraException) {
                e.printStackTrace()

                null
            }
            return singleton != null
        }
    }
}
