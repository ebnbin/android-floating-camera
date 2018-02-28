/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// https://github.com/googlesamples/android-Camera2Video

package com.ebnbin.floatingcamera.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.ebnbin.floatingcamera.util.AppUtilsKt;
import com.ebnbin.floatingcamera.util.CameraHelper;
import com.ebnbin.floatingcamera.util.PreferenceHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCamera2VideoTextureView extends CameraView {

    private void onClick() {
        if (mIsRecordingVideo) {
            stopRecordingVideo();
        } else {
            startRecordingVideo();
        }
    }

    private void record() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isNotAttachedToWindow()) {
                    return;
                }

                onClick();
            }
        }, 1000L);
    }

    private void stop() {
        if (!mIsRecordingVideo) {
            return;
        }

        onClick();
    }

    private void runOnUiThread(final Runnable action) {
        post(new Runnable() {
            @Override
            public void run() {
                if (isNotAttachedToWindow()) {
                    return;
                }

                action.run();
            }
        });
    }

    //*****************************************************************************************************************

    @Override
    protected void beforeOpenCamera() {
        super.beforeOpenCamera();

        mMediaRecorder = new MediaRecorder();
    }

    @Override
    protected void onOpened() {
        super.onOpened();

        startPreview();

        record();
    }

    @Override
    protected void onCloseCamera() {
        super.onCloseCamera();

        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    protected void beforeFinish() {
        super.beforeFinish();

        stop();
    }

    //*****************************************************************************************************************

    public JCamera2VideoTextureView(Context context) {
        this(context, null);
    }

    public JCamera2VideoTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCamera2VideoTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //*****************************************************************************************************************

    private static final String TAG = "JCamera2VideoTextureVie";

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    private File mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        CameraHelper.Device.Resolution previewResolution = getPreviewResolution();

        if (null == getCameraDevice() || !isAvailable()) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewResolution.getWidth(), previewResolution.getHeight());
            mPreviewBuilder = getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            getCameraDevice().createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
//                            Activity activity = getActivity();
//                            if (null != activity) {
//                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
//                            }
                            toast("Failed");
                        }
                    }, getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == getCameraDevice()) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void setUpMediaRecorder() throws IOException {
        if (isNotAttachedToWindow()) {
            return;
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        if (mNextVideoAbsolutePath == null) {
            mNextVideoAbsolutePath = getVideoFilePath();
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath.getAbsolutePath());
        mMediaRecorder.setOrientationHint(getDevice().getOrientation(AppUtilsKt.displayRotation()));

        CameraHelper.Device.VideoProfile videoProfile = PreferenceHelper.INSTANCE.videoProfile();
        if (videoProfile == null) {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(getResolution().getWidth(), getResolution().getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        } else {
            mMediaRecorder.setProfile(videoProfile.getCamcorderProfile());
        }

        mMediaRecorder.prepare();
    }

    private File getVideoFilePath() {
        return new File(PreferenceHelper.INSTANCE.path(), "" + System.currentTimeMillis() + ".mp4");
    }

    private void startRecordingVideo() {
        CameraHelper.Device.Resolution previewResolution = getPreviewResolution();

        if (null == getCameraDevice() || !isAvailable()) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewResolution.getWidth(), previewResolution.getHeight());
            mPreviewBuilder = getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            getCameraDevice().createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    /*getActivity().*/runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
//                            mButtonVideo.setText(/*R.string.stop*/"Stop");
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Activity activity = getActivity();
//                    if (null != activity) {
//                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
//                    }
                    toast("Failed");
                }
            }, getBackgroundHandler());
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
//        mButtonVideo.setText(/*R.string.record*/"Record");
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

//        Activity activity = getActivity();
        if (isAttachedToWindow()) {
//            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
//                    Toast.LENGTH_SHORT).show();
            toast("Video saved: " + mNextVideoAbsolutePath);
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        mNextVideoAbsolutePath = null;
//        startPreview();
    }

}
