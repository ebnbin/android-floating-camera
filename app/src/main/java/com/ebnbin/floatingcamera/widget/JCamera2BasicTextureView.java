/*
 * Copyright 2017 The Android Open Source Project
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

// https://github.com/googlesamples/android-Camera2Basic

package com.ebnbin.floatingcamera.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;

import com.ebnbin.floatingcamera.util.AppUtilsKt;

import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class JCamera2BasicTextureView extends CameraView {

    @Override
    public boolean onSingleTapConfirmed(@Nullable MotionEvent e) {
        boolean result = super.onSingleTapConfirmed(e);
        post(new Runnable() {
            @Override
            public void run() {
                if (isNotAttachedToWindow()) {
                    return;
                }

                captureStillPicture();
            }
        });
        return result;
    }

    @Override
    protected void beforeOpenCamera() {
        super.beforeOpenCamera();

        mImageReader = ImageReader.newInstance(getResolution().getWidth(), getResolution().getHeight(),
                ImageFormat.JPEG, /*maxImages*/2);
        mImageReader.setOnImageAvailableListener(
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(final ImageReader reader) {
                        getBackgroundHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Image image = reader.acquireNextImage();

                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                FileOutputStream output = null;
                                try {
                                    output = new FileOutputStream(getFile());
                                    output.write(bytes);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    image.close();
                                    if (null != output) {
                                        try {
                                            output.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                    }
                }, getBackgroundHandler());
    }

    @Override
    protected void onOpened() {
        super.onOpened();

        createCameraPreviewSession();
    }

    @Override
    protected void onCloseCamera() {
        super.onCloseCamera();

        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    //*****************************************************************************************************************

    public JCamera2BasicTextureView(Context context) {
        this(context, null);
    }

    public JCamera2BasicTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCamera2BasicTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //*****************************************************************************************************************

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * {@link CaptureRequest} generated by previewRequestBuilder.
     */
    private CaptureRequest mPreviewRequest;

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        if (null == getCameraDevice() || !isAvailable() || isNotAttachedToWindow()) {
            return;
        }
        try {
            // This is the output Surface we need to start preview.
            Surface surface = new Surface(getSurfaceTexture());

            // We set up a CaptureRequest.Builder with the output Surface.
            final CaptureRequest.Builder previewRequestBuilder
                    = getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            getCameraDevice().createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == getCameraDevice()) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = previewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, getBackgroundHandler());
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            toast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture.
     */
    private void captureStillPicture() {
        try {
            if (isNotAttachedToWindow() || null == getCameraDevice()) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    getDevice().getOrientation(AppUtilsKt.displayRotation()));

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                        long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                    setUpFile(".jpg");
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull TotalCaptureResult result) {
                    toastFile();

                    try {
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, null,
                                getBackgroundHandler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
