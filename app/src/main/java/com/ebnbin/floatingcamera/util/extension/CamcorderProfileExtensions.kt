package com.ebnbin.floatingcamera.util.extension

import android.media.CamcorderProfile
import android.media.MediaRecorder

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
 * [CamcorderProfile.quality] 字符串.
 */
val CamcorderProfile.qualityString get() = when (quality) {
    CamcorderProfile.QUALITY_LOW -> "LOW"
    CamcorderProfile.QUALITY_HIGH -> "HIGH"
    CamcorderProfile.QUALITY_QCIF -> "QCIF"
    CamcorderProfile.QUALITY_CIF -> "CIF"
    CamcorderProfile.QUALITY_480P -> "480P"
    CamcorderProfile.QUALITY_720P -> "720P"
    CamcorderProfile.QUALITY_1080P -> "1080P"
    CamcorderProfile.QUALITY_QVGA -> "QVGA"
    CamcorderProfile.QUALITY_2160P -> "2160P"
    CamcorderProfile.QUALITY_TIME_LAPSE_LOW -> "TIME_LAPSE_LOW"
    CamcorderProfile.QUALITY_TIME_LAPSE_HIGH -> "TIME_LAPSE_HIGH"
    CamcorderProfile.QUALITY_TIME_LAPSE_QCIF -> "TIME_LAPSE_QCIF"
    CamcorderProfile.QUALITY_TIME_LAPSE_CIF -> "TIME_LAPSE_CIF"
    CamcorderProfile.QUALITY_TIME_LAPSE_480P -> "TIME_LAPSE_480P"
    CamcorderProfile.QUALITY_TIME_LAPSE_720P -> "TIME_LAPSE_720P"
    CamcorderProfile.QUALITY_TIME_LAPSE_1080P -> "TIME_LAPSE_1080P"
    CamcorderProfile.QUALITY_TIME_LAPSE_QVGA -> "TIME_LAPSE_QVGA"
    CamcorderProfile.QUALITY_TIME_LAPSE_2160P -> "TIME_LAPSE_2160P"
    CamcorderProfile.QUALITY_HIGH_SPEED_LOW -> "HIGH_SPEED_LOW"
    CamcorderProfile.QUALITY_HIGH_SPEED_HIGH -> "HIGH_SPEED_HIGH"
    CamcorderProfile.QUALITY_HIGH_SPEED_480P -> "HIGH_SPEED_480P"
    CamcorderProfile.QUALITY_HIGH_SPEED_720P -> "HIGH_SPEED_720P"
    CamcorderProfile.QUALITY_HIGH_SPEED_1080P -> "HIGH_SPEED_1080P"
    CamcorderProfile.QUALITY_HIGH_SPEED_2160P -> "HIGH_SPEED_2160P"
    else -> "?"
}

/**
 * [CamcorderProfile.fileFormat] 字符串.
 */
val CamcorderProfile.fileFormatString get() = when (fileFormat) {
    MediaRecorder.OutputFormat.DEFAULT -> "DEFAULT"
    MediaRecorder.OutputFormat.THREE_GPP -> "THREE_GPP"
    MediaRecorder.OutputFormat.MPEG_4 -> "MPEG_4"
    MediaRecorder.OutputFormat.AMR_NB -> "AMR_NB"
    MediaRecorder.OutputFormat.AMR_WB -> "AMR_WB"
    MediaRecorder.OutputFormat.AAC_ADTS -> "AAC_ADTS"
    MediaRecorder.OutputFormat.MPEG_2_TS -> "MPEG_2_TS"
    MediaRecorder.OutputFormat.WEBM -> "WEBM"
    else -> "?"
}

/**
 * [CamcorderProfile] 文件格式后缀名.
 */
val CamcorderProfile.fileFormatExtension get() = when (fileFormat) {
    MediaRecorder.OutputFormat.DEFAULT -> ""
    MediaRecorder.OutputFormat.THREE_GPP -> ".3gp"
    MediaRecorder.OutputFormat.MPEG_4 -> ".mp4"
    MediaRecorder.OutputFormat.AMR_NB -> ".amr"
    MediaRecorder.OutputFormat.AMR_WB -> ".awb"
    MediaRecorder.OutputFormat.AAC_ADTS -> ".aac"
    MediaRecorder.OutputFormat.MPEG_2_TS -> ".aac"
    MediaRecorder.OutputFormat.WEBM -> ".webm"
    else -> ""
}

/**
 * [CamcorderProfile.videoCodec] 字符串.
 */
val CamcorderProfile.videoCodecString get() = when (videoCodec) {
    MediaRecorder.VideoEncoder.DEFAULT -> "DEFAULT"
    MediaRecorder.VideoEncoder.H263 -> "H263"
    MediaRecorder.VideoEncoder.H264 -> "H264"
    MediaRecorder.VideoEncoder.MPEG_4_SP -> "MPEG_4_SP"
    MediaRecorder.VideoEncoder.VP8 -> "VP8"
    MediaRecorder.VideoEncoder.HEVC -> "HEVC"
    else -> "?"
}

/**
 * [CamcorderProfile.audioCodec] 字符串.
 */
val CamcorderProfile.audioCodecString get() = when (audioCodec) {
    MediaRecorder.AudioEncoder.DEFAULT -> "DEFAULT"
    MediaRecorder.AudioEncoder.AMR_NB -> "AMR_NB"
    MediaRecorder.AudioEncoder.AMR_WB -> "AMR_WB"
    MediaRecorder.AudioEncoder.AAC -> "AAC"
    MediaRecorder.AudioEncoder.HE_AAC -> "HE_AAC"
    MediaRecorder.AudioEncoder.AAC_ELD -> "AAC_ELD"
    MediaRecorder.AudioEncoder.VORBIS -> "VORBIS"
    else -> "?"
}
