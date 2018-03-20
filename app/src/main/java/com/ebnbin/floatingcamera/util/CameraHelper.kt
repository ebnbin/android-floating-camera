package com.ebnbin.floatingcamera.util

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.Size
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.extension.audioCodecString
import com.ebnbin.floatingcamera.util.extension.extensionEquals
import com.ebnbin.floatingcamera.util.extension.extensionHashCode
import com.ebnbin.floatingcamera.util.extension.fileFormatString
import com.ebnbin.floatingcamera.util.extension.gcd
import com.ebnbin.floatingcamera.util.extension.qualityString
import com.ebnbin.floatingcamera.util.extension.videoCodecString
import kotlin.math.min

/**
 * 相机帮助类.
 *
 * @throws CameraException
 */
class CameraHelper private constructor() {
    /**
     * Id.
     */
    private val ids: Array<String> = try {
        cameraManager.cameraIdList
    } catch (e: Exception) {
        throw when (e) {
            is CameraAccessException, is NullPointerException -> {
                CameraException("Id 获取失败.", e)
            }
            else -> e
        }
    }

    /**
     * Id 数量.
     */
    private val idSize = ids.size
    init {
        if (idSize <= 0) throw CameraException("Id 数量为 0.")
    }

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
            try {
                val device = Device(ids[index], index, hasBackDevice, hasFrontDevice)
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
            }
        }

        this.hasBackDevice = hasBackDevice
        this.hasFrontDevice = hasFrontDevice
        if (!hasBackDevice && !hasFrontDevice) throw BaseRuntimeException()

        hasBothDevices = this.hasBackDevice && this.hasFrontDevice

        // TODO: 如果摄像头发生变化, 需要删除 "is_front" 偏好.
    }

    /**
     * 摄像头.
     *
     * @param id Id.
     *
     * @param idIndex Camera1 id.
     *
     * @param hasBackDevice 是否已检测到后置摄像头. 如果为 `true` 且当前摄像头也是后置摄像头则抛出 [CameraException].
     *
     * @param hasFrontDevice 是否已检测到前置摄像头. 如果为 `true` 且当前摄像头也是前置摄像头则抛出 [CameraException].
     */
    class Device(val id: String, private val idIndex: Int, hasBackDevice: Boolean, hasFrontDevice: Boolean) {
        /**
         * [CameraCharacteristics].
         */
        private val cameraCharacteristics = try {
            cameraManager.getCameraCharacteristics(id) ?: throw CameraException("CameraCharacteristics 获取失败.")
        } catch (e: Exception) {
            throw when (e) {
                is CameraAccessException, is NullPointerException -> {
                    CameraException("CameraCharacteristics 获取失败.", e)
                }
                else -> e
            }
        }

        /**
         * 朝向.
         */
        private val lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                ?: throw CameraException("朝向获取失败.")

        init {
            when (lensFacing) {
                CameraMetadata.LENS_FACING_FRONT -> {
                    if (hasFrontDevice) throw CameraException("超过 1 个前置摄像头.")
                }
                CameraMetadata.LENS_FACING_BACK -> {
                    if (hasBackDevice) throw CameraException("超过 1 个后置摄像头.")
                }
                CameraMetadata.LENS_FACING_EXTERNAL -> throw CameraException("不支持外置摄像头.")
                else -> throw BaseRuntimeException()
            }
        }

        /**
         * 是否为前置摄像头.
         */
        val isFront = lensFacing == CameraMetadata.LENS_FACING_FRONT

        /**
         * 传感器方向.
         */
        private val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                ?: throw CameraException("传感器方向获取失败.")

        /**
         * 传感器方向是否为横向.
         */
        private val isSensorOrientationLandscape = sensorOrientation == 90 || sensorOrientation == 270

        /**
         * 获取拍摄方向.
         */
        fun getOrientation(rotation: Int = displayRotation()) =
                if(isFront)
                    (sensorOrientation + (90 * rotation) + 360) % 360 else
                    (sensorOrientation - (90 * rotation) + 360) % 360

        /**
         * 闪光灯是否可用.
         */
        private val flashInfoAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                ?: throw CameraException("闪光灯是否可用获取失败.")

        /**
         * 闪光灯模式.
         */
        private val controlAeAvailableModes = cameraCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES) ?: throw CameraException("闪光灯模式获取失败.")

        /**
         * [StreamConfigurationMap].
         */
        private val scalerStreamConfigurationMap = cameraCharacteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?: throw CameraException("StreamConfigurationMap 获取失败.")

        /**
         * [SurfaceTexture] 输出尺寸列表.
         */
        private val surfaceTextureSizes: Array<Size> = scalerStreamConfigurationMap.getOutputSizes(
                SurfaceTexture::class.java) ?: throw CameraException("SurfaceTexture 输出尺寸列表获取失败.")
        /**
         * [MediaRecorder] 输出尺寸列表.
         */
        private val mediaRecorderSizes: Array<Size> = scalerStreamConfigurationMap.getOutputSizes(
                MediaRecorder::class.java) ?: throw CameraException("MediaRecorder 输出尺寸列表获取失败.")
        /**
         * [ImageFormat.JPEG] 输出尺寸列表.
         */
        private val jpegSizes: Array<Size> = scalerStreamConfigurationMap.getOutputSizes(ImageFormat.JPEG)
                ?: throw CameraException("JPEG 输出尺寸列表获取失败.")

        /**
         * [CamcorderProfile] 列表.
         */
        private val camcorderProfiles: Array<CamcorderProfile>
        init {
            val camcorderProfileList = ArrayList<CamcorderProfile>()
            CAMCORDER_PROFILE_QUALITIES
                    .filter { CamcorderProfile.hasProfile(idIndex, it) }
                    .mapTo(camcorderProfileList) { CamcorderProfile.get(idIndex, it)!! }

            if (camcorderProfileList.isEmpty()) throw CameraException("CamcorderProfile 数量为 0.")

            camcorderProfiles = camcorderProfileList.toTypedArray()
        }

        /**
         * 预览分辨率列表.
         */
        private val previewResolutions = createResolutions(
                { throw CameraException("预览分辨率数量为 0.") },
                surfaceTextureSizes)
        /**
         * 视频分辨率列表.
         */
        val videoResolutions = createResolutions(
                { throw CameraException("视频分辨率数量为 0.") },
                mediaRecorderSizes)
        /**
         * 照片分辨率列表.
         */
        val photoResolutions = createResolutions(
                { throw CameraException("照片分辨率数量为 0.") },
                jpegSizes)

        /**
         * 创建分辨率数组.
         * 从大到小排序.
         *
         * @param onEmpty 分辨率数量为 0 的回调.
         *
         * @param sizes 尺寸列表.
         */
        private fun createResolutions(
                onEmpty: () -> Unit,
                sizes: Array<Size>): Array<Resolution> {
            fun createResolution(width: Int, height: Int) =
                    Resolution(width, height, isSensorOrientationLandscape, camcorderProfiles)

            val resolutionList = ArrayList<Resolution>()
            sizes.forEach { resolutionList.add(createResolution(it.width, it.height)) }

            if (resolutionList.isEmpty()) onEmpty()

            resolutionList.sortDescending()

            return resolutionList.toTypedArray()
        }

        /**
         * 视频分辨率摘要列表.
         */
        val videoResolutionSummaries = Array(videoResolutions.size) { videoResolutions[it].videoSummary }
        /**
         * 照片分辨率摘要列表.
         */
        val photoResolutionSummaries = Array(photoResolutions.size) { photoResolutions[it].photoSummary }

        /**
         * 最大分辨率. 取照片分辨率最大值.
         */
        private val maxResolution = photoResolutions.first()

        /**
         * 预览分辨率.
         *
         * 1. 与 [maxResolution] 宽高比相同.
         * 2. 小等于 `1920x1080` 与屏幕宽高取小值.
         */
        val previewResolution: Resolution
        init {
            fun getPreviewResolution(): Resolution {
                val previewResolutionList = ArrayList<Resolution>()
                previewResolutions.filterTo(previewResolutionList) { it.isRatioEquals(maxResolution) }

                val maxLandscapeWidth = min(1920, displayRealSize.landscapeWidth)
                val maxLandscapeHeight = min(1080, displayRealSize.landscapeHeight)
                val previewResolutionList2 = ArrayList<Resolution>()
                if (previewResolutionList.isEmpty()) {
                    previewResolutions.filterTo(previewResolutionList2) {
                        it.isLessOrEquals(maxLandscapeWidth, maxLandscapeHeight)
                    }

                    return if (previewResolutionList2.isEmpty())
                        previewResolutions.first() else
                        previewResolutionList2.first()
                } else {
                    previewResolutionList.filterTo(previewResolutionList2) {
                        it.isLessOrEquals(maxLandscapeWidth, maxLandscapeHeight)
                    }

                    return if (previewResolutionList2.isEmpty())
                        previewResolutionList.first() else
                        previewResolutionList2.first()
                }
            }

            previewResolution = getPreviewResolution()
        }

        /**
         * 视频配置列表. 从大到小排序.
         */
        val videoProfiles: Array<VideoProfile>
        init {
            val videoProfileList = ArrayList<VideoProfile>()
            for (camcorderProfile in camcorderProfiles) {
                try {
                    val videoProfile = VideoProfile(camcorderProfile, camcorderProfiles, isSensorOrientationLandscape,
                            videoResolutions)
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
         * 视频配置摘要列表. 在底部添加 "自定义配置".
         */
        val videoProfileSummaries: Array<String>
        init {
            val videoProfileSummaryList = ArrayList<String>()
            videoProfiles.mapTo(videoProfileSummaryList) { it.summary }
            videoProfileSummaryList.add(getString(R.string.video_profile_summary_custom))

            videoProfileSummaries = videoProfileSummaryList.toTypedArray()
        }

        /**
         * 分辨率.
         *
         * @param width 宽.
         *
         * @param height 高.
         *
         * @param isSensorOrientationLandscape 传感器方向是否为横向.
         *
         * @param camcorderProfiles [CamcorderProfile] 列表.
         */
        class Resolution(val width: Int, val height: Int, private val isSensorOrientationLandscape: Boolean,
                camcorderProfiles: Array<CamcorderProfile>) :
                Comparable<Resolution> {
            /**
             * 宽高最大公约数.
             */
            private val gcd = width gcd height

            /**
             * 宽比.
             */
            private val ratioWidth = if (gcd == 0) width else width / gcd
            /**
             * 高比.
             */
            private val ratioHeight = if (gcd == 0) height else height / gcd

            /**
             * 面积.
             */
            private val area = width.toLong() * height

            /**
             * 百万像素.
             */
            private val megapixels = area.toFloat() / 1_000_000f

            /**
             * 视频摘要.
             */
            val videoSummary: String
            init {
                val qualityString = camcorderProfiles
                        .firstOrNull { width == it.videoFrameWidth && height == it.videoFrameHeight }
                        ?.qualityString
                        ?: ""
                val qualitySummary = if (qualityString.isEmpty())
                    "" else
                    resources.getString(R.string.video_resolution_summary_quality, qualityString)

                videoSummary = resources.getString(R.string.video_resolution_summary, this.width, this.height,
                        ratioWidth, ratioHeight, megapixels, qualitySummary)!!
            }

            /**
             * 照片摘要.
             */
            val photoSummary = resources.getString(R.string.photo_resolution_summary, this.width, this.height,
                    ratioWidth, ratioHeight, megapixels)!!

            /**
             * [Size].
             */
            val size = Size(width, height)

            /**
             * 横向宽.
             */
            val landscapeWidth = if (isSensorOrientationLandscape) width else height
            /**
             * 横向高.
             */
            val landscapeHeight = if (isSensorOrientationLandscape) height else width

            /**
             * 纵向宽.
             */
            val portraitWidth = landscapeHeight
            /**
             * 纵向高.
             */
            val portraitHeight = landscapeWidth

            fun width(rotation: Int = displayRotation()) = when (rotation) {
                0, 2 -> portraitWidth
                1, 3 -> landscapeWidth
                else -> throw BaseRuntimeException()
            }

            fun height(rotation: Int = displayRotation()) = when (rotation) {
                0, 2 -> portraitHeight
                1, 3 -> landscapeHeight
                else -> throw BaseRuntimeException()
            }

            /**
             * 宽高比是否相同.
             */
            fun isRatioEquals(other: Resolution) = ratioWidth == other.ratioWidth && ratioHeight == other.ratioHeight

            /**
             * 小等于.
             */
            fun isLessOrEquals(landscapeWidth: Int, landscapeHeight: Int) =
                    this.landscapeWidth <= landscapeWidth && this.landscapeHeight <= landscapeHeight

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
         * 视频配置. [CamcorderProfile] 的帮助类.
         *
         * @param camcorderProfile [CamcorderProfile].
         *
         * @param camcorderProfiles [CamcorderProfile] 列表.
         *
         * @param isSensorOrientationLandscape 传感器方向是否为横向.
         *
         * @param videoResolutions 视频分辨率列表.
         *
         * @throws CameraException
         */
        class VideoProfile(
                val camcorderProfile: CamcorderProfile,
                camcorderProfiles: Array<CamcorderProfile>,
                isSensorOrientationLandscape: Boolean,
                videoResolutions: Array<Resolution>):
                Comparable<VideoProfile> {
            val videoResolution = Resolution(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight,
                    isSensorOrientationLandscape, camcorderProfiles)
            init {
                if (!videoResolutions.contains(videoResolution)) throw CameraException("CamcorderProfile 分辨率不支持.")
            }

            /**
             * 摘要.
             */
            val summary = "duration=${camcorderProfile.duration}, " +
                    "quality=${camcorderProfile.qualityString}, " +
                    "fileFormat=${camcorderProfile.fileFormatString}, " +
                    "videoCodec=${camcorderProfile.videoCodecString}, " +
                    "videoBitRate=${camcorderProfile.videoBitRate}, " +
                    "videoFrameRate=${camcorderProfile.videoFrameRate}, " +
                    "videoFrameWidth=${camcorderProfile.videoFrameWidth}, " +
                    "videoFrameHeight=${camcorderProfile.videoFrameHeight}, " +
                    "audioCodec=${camcorderProfile.audioCodecString}, " +
                    "audioBitRate=${camcorderProfile.audioBitRate}, " +
                    "audioSampleRate=${camcorderProfile.audioSampleRate}, " +
                    "audioChannels=${camcorderProfile.audioChannels}"

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
         *
         * @param force 如果已经检测过, 只在 [force] 为 true 才重新检测.
         */
        fun detect(force: Boolean = false): Boolean {
            if (singleton != null && !force) return true

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
