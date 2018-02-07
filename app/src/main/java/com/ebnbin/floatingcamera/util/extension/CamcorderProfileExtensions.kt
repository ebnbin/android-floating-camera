package com.ebnbin.floatingcamera.util.extension

import android.media.CamcorderProfile
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.getString

/**
 * 扩展 [CamcorderProfile.equals]. 不比较 [CamcorderProfile.quality].
 */
fun CamcorderProfile.extensionEquals(other: CamcorderProfile): Boolean {
    if (this === other) return true

    return duration == other.duration &&
            fileFormat == other.fileFormat &&
            videoCodec == other.videoCodec &&
            videoBitRate == other.videoBitRate &&
            videoFrameRate == other.videoFrameRate &&
            videoFrameWidth == other.videoFrameWidth &&
            videoFrameHeight == other.videoFrameHeight &&
            audioCodec == other.audioCodec &&
            audioBitRate == other.audioBitRate &&
            audioSampleRate == other.audioSampleRate &&
            audioChannels == other.audioChannels
}

/**
 * 扩展 [CamcorderProfile.hashCode]. 不计算 [CamcorderProfile.quality].
 */
fun CamcorderProfile.extensionHashCode(): Int {
    var result = duration.hashCode()
    result = 31 * result + fileFormat.hashCode()
    result = 31 * result + videoCodec.hashCode()
    result = 31 * result + videoBitRate.hashCode()
    result = 31 * result + videoFrameRate.hashCode()
    result = 31 * result + videoFrameWidth.hashCode()
    result = 31 * result + videoFrameHeight.hashCode()
    result = 31 * result + audioCodec.hashCode()
    result = 31 * result + audioBitRate.hashCode()
    result = 31 * result + audioSampleRate.hashCode()
    result = 31 * result + audioChannels.hashCode()
    return result
}

/**
 * [CamcorderProfile.quality] 字符串. 只支持 [CamcorderProfile.QUALITY_LOW] 到 [CamcorderProfile.QUALITY_2160P].
 */
val CamcorderProfile.qualityString get() = getString(when (quality) {
    CamcorderProfile.QUALITY_2160P -> R.string.camcorder_profile_quality_2160p
    CamcorderProfile.QUALITY_1080P -> R.string.camcorder_profile_quality_1080p
    CamcorderProfile.QUALITY_720P -> R.string.camcorder_profile_quality_720p
    CamcorderProfile.QUALITY_480P -> R.string.camcorder_profile_quality_480p
    CamcorderProfile.QUALITY_CIF -> R.string.camcorder_profile_quality_cif
    CamcorderProfile.QUALITY_QVGA -> R.string.camcorder_profile_quality_qvga
    CamcorderProfile.QUALITY_QCIF -> R.string.camcorder_profile_quality_qcif
    CamcorderProfile.QUALITY_HIGH -> R.string.camcorder_profile_quality_high
    CamcorderProfile.QUALITY_LOW -> R.string.camcorder_profile_quality_low
    else -> throw BaseRuntimeException()
})
