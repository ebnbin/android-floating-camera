@file:Suppress("DEPRECATION")

package com.ebnbin.floatingcamera.util

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.CamcorderProfile
import android.util.Size
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.extension.extensionEquals
import com.ebnbin.floatingcamera.util.extension.extensionHashCode
import com.ebnbin.floatingcamera.util.extension.gcd
import com.ebnbin.floatingcamera.util.extension.qualityString
import kotlin.math.min

/**
 * 相机帮助类.
 *
 * @throws CameraException
 */
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
        if (!hasBackDevice && !hasFrontDevice) throw BaseRuntimeException()

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
    class Device(val id2: String, private val id1: Int, camera1: Camera, hasBackDevice: Boolean,
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
         * Camera1 [Camera.Parameters].
         */
        private val parameters1 = camera1.parameters ?: throw CameraException("Camera1 Parameters 获取失败.")

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

        /**
         * Camera2 [StreamConfigurationMap].
         */
        private val scalerStreamConfigurationMap2 = cameraCharacteristics2.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?: throw CameraException("Camera2 StreamConfigurationMap 获取失败.")

        /**
         * Camera2 [SurfaceTexture] 输出尺寸列表.
         */
        private val surfaceTextureSizes2: Array<Size> = scalerStreamConfigurationMap2.getOutputSizes(
                SurfaceTexture::class.java) ?: throw CameraException("Camera2 SurfaceTexture 输出尺寸列表获取失败.")

        /**
         * Camera1 预览尺寸列表.
         */
        private val supportedPreviewSizes1: List<Camera.Size> = parameters1.supportedPreviewSizes
                ?: throw CameraException("Camera1 预览尺寸列表获取失败.")
        /**
         * Camera1 视频尺寸列表.
         */
        private val supportedVideoSizes1: List<Camera.Size>? = parameters1.supportedVideoSizes
        /**
         * Camera1 照片尺寸列表.
         */
        private val supportedPictureSizes1: List<Camera.Size> = parameters1.supportedPictureSizes
                ?: throw CameraException("Camera1 照片尺寸列表获取失败.")

        /**
         * [CamcorderProfile] 列表.
         */
        private val camcorderProfiles: Array<CamcorderProfile>
        init {
            val camcorderProfileList = ArrayList<CamcorderProfile>()
            CAMCORDER_PROFILE_QUALITIES
                    .filter { CamcorderProfile.hasProfile(id1, it) }
                    .mapTo(camcorderProfileList) { CamcorderProfile.get(id1, it)!! }

            if (camcorderProfileList.isEmpty()) throw CameraException("CamcorderProfile 数量为 0.")

            camcorderProfiles = camcorderProfileList.toTypedArray()
        }

        /**
         * 预览分辨率列表.
         */
        private val previewResolutions = createResolutions(
                { width, height -> PreviewResolution(width, height) },
                { throw CameraException("预览分辨率数量为 0.") },
                surfaceTextureSizes2,
                supportedPreviewSizes1)
        /**
         * 视频分辨率列表.
         */
        private val videoResolutions = createResolutions(
                { width, height -> VideoResolution(width, height, camcorderProfiles) },
                { throw CameraException("视频分辨率数量为 0.") },
                surfaceTextureSizes2,
                supportedVideoSizes1,
                supportedPreviewSizes1)
        /**
         * 照片分辨率列表.
         */
        private val photoResolutions = createResolutions(
                { width, height -> PhotoResolution(width, height) },
                { throw CameraException("照片分辨率数量为 0.") },
                surfaceTextureSizes2,
                supportedPictureSizes1)

        /**
         * 创建分辨率数组. 取 [sizes2] 与 [sizes1] 分辨率交集, 如果 [sizes1] 为 `null` 则使用 [alternativeSizes1].
         * 从大到小排序.
         *
         * @param createResolution 创建分辨率. 参数 `width`, `height`.
         *
         * @param onEmpty 分辨率数量为 0 的回调.
         *
         * @param sizes2 Camera2 尺寸列表.
         *
         * @param sizes1 Camera1 尺寸列表.
         *
         * @param alternativeSizes1 如果 [sizes1] 为 `null`, 使用这个参数.
         */
        private fun <T : Resolution> createResolutions(
                createResolution: (Int, Int) -> T,
                onEmpty: () -> Unit,
                sizes2: Array<Size>,
                sizes1: List<Camera.Size>?,
                alternativeSizes1: List<Camera.Size>? = null): Array<Resolution> {
            val resolutionList2 = ArrayList<Resolution>()
            sizes2.forEach { resolutionList2.add(createResolution(it.width, it.height)) }

            val resolutionList1 = ArrayList<Resolution>()
            if (sizes1 == null) {
                alternativeSizes1?.forEach { resolutionList1.add(createResolution(it.width, it.height)) }
            } else {
                sizes1.forEach { resolutionList1.add(createResolution(it.width, it.height)) }
            }

            resolutionList2.retainAll(resolutionList1)

            if (resolutionList2.isEmpty()) onEmpty()

            resolutionList2.sortDescending()

            return resolutionList2.toTypedArray()
        }

        /**
         * 视频分辨率摘要列表.
         */
        val videoResolutionSummaries = Array(videoResolutions.size) { videoResolutions[it].summary }
        /**
         * 照片分辨率摘要列表.
         */
        val photoResolutionSummaries = Array(photoResolutions.size) { photoResolutions[it].summary }

        /**
         * 视频配置列表. 从大到小排序.
         */
        private val videoProfiles: Array<VideoProfile>
        init {
            val videoProfileList = ArrayList<VideoProfile>()
            for (camcorderProfile in camcorderProfiles) {
                try {
                    val videoProfile = VideoProfile(camcorderProfile, camcorderProfiles, videoResolutions)
                    if (!videoProfileList.contains(videoProfile)) videoProfileList.add(videoProfile)
                } catch (e: CameraException) {
                    e.printStackTrace()
                }
            }

            if (videoProfileList.isEmpty()) throw CameraException("视频配置数量为 0.")

            videoProfileList.sortDescending()

            videoProfiles = videoProfileList.toTypedArray()
        }

        /**
         * 分辨率.
         *
         * @param width 宽.
         *
         * @param height 高.
         */
        abstract class Resolution(val width: Int, val height: Int) : Comparable<Resolution> {
            /**
             * 宽高最大公约数.
             */
            private val gcd = width gcd height

            /**
             * 宽比.
             */
            protected val ratioWidth = if (gcd == 0) width else width / gcd
            /**
             * 高比.
             */
            protected val ratioHeight = if (gcd == 0) height else height / gcd

            /**
             * 面积.
             */
            private val area = width.toLong() * height

            /**
             * 百万像素.
             */
            protected val megapixels = area.toFloat() / 1_000_000f

            /**
             * 摘要.
             */
            abstract val summary: String

            override fun equals(other: Any?): Boolean {
                if (this === other) return true

                if (javaClass != other?.javaClass) return false

                other as Resolution

                return width == other.width && height == other.height
            }

            override fun hashCode(): Int {
                var result = width
                result = 31 * result + height
                return result
            }

            /**
             * 面积优先, 宽度其次.
             */
            override fun compareTo(other: Resolution) = compareValuesBy(this, other, Resolution::area,
                    Resolution::width)
        }

        /**
         * 预览分辨率.
         */
        class PreviewResolution(width: Int, height: Int) : Resolution(width, height) {
            override val summary = resources.getString(R.string.preview_resolution_summary, this.width, this.height,
                    ratioWidth, ratioHeight)!!
        }

        /**
         * 视频分辨率.
         */
        class VideoResolution(width: Int, height: Int, camcorderProfiles: Array<CamcorderProfile>) :
                Resolution(width, height) {
            override val summary: String
            init {
                val qualityString = camcorderProfiles
                        .firstOrNull { width == it.videoFrameWidth && height == it.videoFrameHeight }
                        ?.qualityString
                        ?: ""
                val qualitySummary = if (qualityString.isEmpty())
                    "" else
                    resources.getString(R.string.video_resolution_summary_quality, qualityString)

                summary = resources.getString(R.string.video_resolution_summary, this.width, this.height, ratioWidth,
                        ratioHeight, megapixels, qualitySummary)!!
            }
        }

        /**
         * 照片分辨率.
         */
        class PhotoResolution(width: Int, height: Int) : Resolution(width, height) {
            override val summary = resources.getString(R.string.photo_resolution_summary, this.width, this.height,
                    ratioWidth, ratioHeight, megapixels)!!
        }

        /**
         * 视频配置. [CamcorderProfile] 的帮助类.
         *
         * @param camcorderProfile [CamcorderProfile].
         *
         * @param camcorderProfiles [CamcorderProfile] 列表.
         *
         * @param videoResolutions 视频分辨率列表.
         *
         * @throws CameraException
         */
        class VideoProfile(private val camcorderProfile: CamcorderProfile, camcorderProfiles: Array<CamcorderProfile>,
                videoResolutions: Array<Resolution>): Comparable<VideoProfile> {
            private val videoResolution = VideoResolution(camcorderProfile.videoFrameWidth,
                    camcorderProfile.videoFrameHeight, camcorderProfiles)
            init {
                if (!videoResolutions.contains(videoResolution)) throw CameraException("CamcorderProfile 分辨率不支持.")
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true

                if (javaClass != other?.javaClass) return false

                other as VideoProfile

                return camcorderProfile.extensionEquals(other.camcorderProfile)
            }

            override fun hashCode() = camcorderProfile.extensionHashCode()

            override fun compareTo(other: VideoProfile) = videoResolution.compareTo(other.videoResolution)
        }

        companion object {
            /**
             * [CamcorderProfile] 质量列表.
             */
            private val CAMCORDER_PROFILE_QUALITIES = arrayOf(
                    CamcorderProfile.QUALITY_2160P,
                    CamcorderProfile.QUALITY_1080P,
                    CamcorderProfile.QUALITY_720P,
                    CamcorderProfile.QUALITY_480P,
                    CamcorderProfile.QUALITY_CIF,
                    CamcorderProfile.QUALITY_QVGA,
                    CamcorderProfile.QUALITY_QCIF,
                    CamcorderProfile.QUALITY_HIGH,
                    CamcorderProfile.QUALITY_LOW)
        }
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
