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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;

import com.ebnbin.floatingcamera.util.AppUtilsKt;
import com.ebnbin.floatingcamera.util.CameraHelper;
import com.ebnbin.floatingcamera.util.PreferenceHelper;
import com.ebnbin.floatingcamera.util.extension.CamcorderProfileExtensionsKt;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCamera2VideoTextureView extends CameraView {

    @Override
    public boolean onSingleTapConfirmed(@Nullable MotionEvent e) {
        boolean result = super.onSingleTapConfirmed(e);
        if (mIsRecordingVideo) {
            stopRecordingVideo(true);
        } else {
            startRecordingVideo();
        }
        return result;
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

    @Override
    protected void beforeOpenCamera() {
        super.beforeOpenCamera();

        mMediaRecorder = new MediaRecorder();
    }

    @Override
    protected void onOpened() {
        super.onOpened();

        startPreview();
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

        if (mIsRecordingVideo) {
            stopRecordingVideo(false);
        }
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

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == getCameraDevice() || !isAvailable()) {
            return;
        }
        try {
            closePreviewSession();

            final Surface previewSurface = new Surface(getSurfaceTexture());

            getCameraDevice().createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (null == getCameraDevice()) {
                                return;
                            }
                            try {
                                CaptureRequest.Builder previewBuilder = getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                previewBuilder.addTarget(previewSurface);
                                previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                session.setRepeatingRequest(previewBuilder.build(), null, getBackgroundHandler());
                                mPreviewSession = session;
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            toast("Failed");
                        }
                    }, getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRecordingVideo() {
        if (null == getCameraDevice() || !isAvailable() || isNotAttachedToWindow()) {
            return;
        }
        try {
            closePreviewSession();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOrientationHint(getDevice().getOrientation(AppUtilsKt.displayRotation()));

            CameraHelper.Device.VideoProfile videoProfile = PreferenceHelper.INSTANCE.videoProfile();
            mMediaRecorder.setProfile(videoProfile.getCamcorderProfile());
            setUpFile(CamcorderProfileExtensionsKt.getFileFormatExtension(videoProfile.getCamcorderProfile()));
            mMediaRecorder.setOutputFile(getFile().getAbsolutePath());

            mMediaRecorder.prepare();

            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            final Surface previewSurface = new Surface(getSurfaceTexture());
            surfaces.add(previewSurface);

            // Set up Surface for the MediaRecorder
            final Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            getCameraDevice().createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == getCameraDevice()) {
                        return;
                    }
                    try {
                        CaptureRequest.Builder previewBuilder = getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        previewBuilder.addTarget(previewSurface);
                        previewBuilder.addTarget(recorderSurface);
                        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), null, getBackgroundHandler());

                        mPreviewSession = cameraCaptureSession;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // UI
                                mIsRecordingVideo = true;

                                // Start recording
                                mMediaRecorder.start();
                            }
                        });
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
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

    private void stopRecordingVideo(boolean resumePreview) {
        // UI
        mIsRecordingVideo = false;
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        if (isAttachedToWindow()) {
            toastFile();
        }
        if (resumePreview) {
            startPreview();
        } else {
            closePreviewSession();
        }
    }

}
