/*
 * Copyright 2015 The Android Open Source Project
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

// https://github.com/googlesamples/android-Camera2Raw

package com.ebnbin.floatingcamera.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.ebnbin.floatingcamera.util.AppUtilsKt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

// TODO: With bugs.
public class JCamera2RawTextureView extends CameraView {

    @Override
    public void onTap() {
        post(new Runnable() {
            @Override
            public void run() {
                captureStillPictureLocked();
            }
        });
    }

    @Override
    protected void beforeOpenCamera() {
        super.beforeOpenCamera();

        setUpCameraOutputs();
    }

    @Override
    protected void onOpened() {
        super.onOpened();

        // Start the preview session if the TextureView has been set up already.
        if (isAvailable()) {
            createCameraPreviewSessionLocked();
        }
    }

    @Override
    protected void onCloseCamera() {
        super.onCloseCamera();

        // Reset state and clean up resources used by the camera.
        // Note: After calling this, the ImageReaders will be closed after any background
        // tasks saving Images from these readers have been completed.
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

    public JCamera2RawTextureView(Context context) {
        this(context, null);
    }

    public JCamera2RawTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCamera2RawTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // *********************************************************************************************
    // State protected by getCameraStateLock().
    //
    // The following state is used across both the UI and background threads.  Methods with "Locked"
    // in the name expect getCameraStateLock() to be held while calling.

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * The {@link CameraCharacteristics} for the currently configured camera device.
     */
    private CameraCharacteristics mCharacteristics;

    /**
     * The {@link ImageReader} that handles JPEG image captures.
     */
    private ImageReader mImageReader;

    /**
     * Whether or not the currently configured camera device is fixed-focus.
     */
    private boolean mNoAFRun = false;

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    //**********************************************************************************************

    /**
     * Sets up state related to camera that is needed before opening a {@link CameraDevice}.
     */
    private void setUpCameraOutputs() {
        try {
            // Configure state.
            CameraCharacteristics characteristics
                    = AppUtilsKt.getCameraManager().getCameraCharacteristics(getDevice().getId());

            // Set up ImageReaders for JPEG outputs.
            mImageReader = ImageReader.newInstance(getResolution().getWidth(),
                    getResolution().getHeight(), ImageFormat.JPEG, /*maxImages*/5);
            mImageReader.setOnImageAvailableListener(
                    new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            dequeueAndSaveImage();
                        }
                    }, getBackgroundHandler());

            mCharacteristics = characteristics;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSessionLocked() {
        try {
            SurfaceTexture texture = getSurfaceTexture();
            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(getPreviewResolution().getWidth(), getPreviewResolution().getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            getCameraDevice().createCaptureSession(Arrays.asList(surface,
                            mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == getCameraDevice()) {
                                return;
                            }

                            try {
                                setup3AControlsLocked(mPreviewRequestBuilder);
                                // Finally, we start displaying the camera preview.
                                cameraCaptureSession.setRepeatingRequest(
                                        mPreviewRequestBuilder.build(),
                                        null, getBackgroundHandler());
                            } catch (CameraAccessException | IllegalStateException e) {
                                e.printStackTrace();
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            toast("Failed to configure camera.");
                        }
                    }, getBackgroundHandler()
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure the given {@link CaptureRequest.Builder} to use auto-focus, auto-exposure, and
     * auto-white-balance controls if available.
     *
     * @param builder the builder to configure.
     */
    private void setup3AControlsLocked(CaptureRequest.Builder builder) {
        // Enable auto-magical 3A run by camera device
        builder.set(CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO);

        Float minFocusDist =
                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
        mNoAFRun = (minFocusDist == null || minFocusDist == 0);

        if (!mNoAFRun) {
            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
            if (contains(mCharacteristics.get(
                            CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (contains(mCharacteristics.get(
                        CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }
    }

    /**
     * Send a capture request to the camera device that initiates a capture targeting the JPEG
     * outputs.
     */
    private void captureStillPictureLocked() {
        try {
            if (isNotAttachedToWindow() || null == getCameraDevice()) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            setup3AControlsLocked(captureBuilder);

            // Set orientation.
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    getDevice().getOrientation(AppUtilsKt.displayRotation()));

            CaptureRequest request = captureBuilder.build();

            mCaptureSession.capture(request, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                        long timestamp, long frameNumber) {
                    setUpFile(".jpg");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                        TotalCaptureResult result) {
                    // Look up the ImageSaverBuilder for this request and update it with the CaptureResult
                    finishedCaptureLocked();

                    toastFile();
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                        CaptureFailure failure) {
                    finishedCaptureLocked();
                    toast("Capture failed!");
                }

            }, getBackgroundHandler());

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after a JPEG capture has completed; resets the AF trigger state for the
     * pre-capture sequence.
     */
    private void finishedCaptureLocked() {
        try {
            // Reset the auto-focus trigger in case AF didn't run quickly enough.
            if (!mNoAFRun) {
                mCaptureSession.capture(mPreviewRequestBuilder.build(), null,
                        getBackgroundHandler());

                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * If all necessary information is available, begin saving the image to a file in a background
     * thread.
     */
    private void dequeueAndSaveImage() {
        // Increment reference count to prevent ImageReader from being closed while we
        // are saving its Images in a background thread (otherwise their resources may
        // be freed while we are writing to a file).
        if (mImageReader == null) {
            Log.e("ebnbin", "Paused the activity before we could save the image," +
                    " ImageReader already closed.");
            return;
        }

        final Image image;
        try {
            image = mImageReader.acquireNextImage();
        } catch (IllegalStateException e) {
            Log.e("ebnbin", "Too many images queued for saving, dropping image.");
            return;
        }

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                int format = image.getFormat();
                if (format == ImageFormat.JPEG) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(getFile());
                        output.write(bytes);
                        success = true;
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
                } else {
                    Log.e("ebnbin", "Cannot save image, unexpected image format:" + format);
                }

                // Decrement reference count to allow ImageReader to be closed to free up resources.
                mImageReader.close();

                // If saving the file succeeded, update MediaStore.
                if (success) {
                    MediaScannerConnection.scanFile(getContext(), new String[]{getFile().getPath()},
                    /*mimeTypes*/null, new MediaScannerConnection.MediaScannerConnectionClient() {
                                @Override
                                public void onMediaScannerConnected() {
                                    // Do nothing
                                }

                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ebnbin", "Scanned " + path + ":");
                                    Log.i("ebnbin", "-> uri=" + uri);
                                }
                            });
                }
            }
        });
    }

    // Utility classes and methods:
    // *********************************************************************************************

    /**
     * Return true if the given array contains the given integer.
     *
     * @param modes array to check.
     * @param mode  integer to get for.
     * @return true if the array contains the given integer, otherwise false.
     */
    private static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }

}
