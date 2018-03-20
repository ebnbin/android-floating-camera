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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.ebnbin.floatingcamera.util.AppUtilsKt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A fragment that demonstrates use of the Camera2 API to capture JPEG photos.
 * <p/>
 * In this example, the lifecycle of a single request to take a photo is:
 * <ul>
 * <li>
 * The user presses the "Picture" button, resulting in a call to {@link #takePicture()}.
 * </li>
 * <li>
 * {@link #takePicture()} initiates a pre-capture sequence that triggers the camera's built-in
 * auto-focus, auto-exposure, and auto-white-balance algorithms (aka. "3A") to run.
 * </li>
 * </ul>
 */
public class JCamera2RawTextureView extends CameraView {

    private void onClick() {
        takePicture();
    }

    private void picture() {
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

    //*****************************************************************************************************************

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

        picture();
    }

    @Override
    protected void onCloseCamera() {
        super.onCloseCamera();

        // Reset state and clean up resources used by the camera.
        // Note: After calling this, the ImageReaders will be closed after any background
        // tasks saving Images from these readers have been completed.
        mPendingUserCaptures = 0;
        mState = STATE_CLOSED;
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

    //*****************************************************************************************************************

    /**
     * Timeout for the pre-capture sequence.
     */
    private static final long PRECAPTURE_TIMEOUT_MS = 1000;

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "JCamera2RawTextureView";

    /**
     * Camera state: Device is closed.
     */
    private static final int STATE_CLOSED = 0;
//
//    /**
//     * Camera state: Device is opened, but is not capturing.
//     */
//    private static final int STATE_OPENED = 1;

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 2;

    /**
     * Camera state: Waiting for 3A convergence before capturing a photo.
     */
    private static final int STATE_WAITING_FOR_3A_CONVERGENCE = 3;

    /**
     * A counter for tracking corresponding {@link CaptureRequest}s and {@link CaptureResult}s
     * across the {@link CameraCaptureSession} capture callbacks.
     */
    private final AtomicInteger mRequestCounter = new AtomicInteger();

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
     * Number of pending user requests to capture a photo.
     */
    private int mPendingUserCaptures = 0;

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * The state of the camera device.
     *
     * @see #mPreCaptureCallback
     */
    private int mState = STATE_CLOSED;

    /**
     * Timer to use with pre-capture sequence to ensure a timely capture if 3A convergence is
     * taking too long.
     */
    private long mCaptureTimer;

    //**********************************************************************************************

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events for the preview and
     * pre-capture sequence.
     */
    private CameraCaptureSession.CaptureCallback mPreCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            synchronized (getCameraStateLock()) {
                switch (mState) {
                    case STATE_PREVIEW: {
                        // We have nothing to do when the camera preview is running normally.
                        break;
                    }
                    case STATE_WAITING_FOR_3A_CONVERGENCE: {
                        boolean readyToCapture = true;
                        if (!mNoAFRun) {
                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == null) {
                                break;
                            }

                            // If auto-focus has reached locked state, we are ready to capture
                            readyToCapture =
                                    (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                            afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
                        }

                        // If we are running on an non-legacy device, we should also wait until
                        // auto-exposure and auto-white-balance have converged as well before
                        // taking a picture.
                        if (!isLegacyLocked()) {
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
                            if (aeState == null || awbState == null) {
                                break;
                            }

                            readyToCapture = readyToCapture &&
                                    aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&
                                    awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
                        }

                        // If we haven't finished the pre-capture sequence but have hit our maximum
                        // wait timeout, too bad! Begin capture anyway.
                        if (!readyToCapture && hitTimeoutLocked()) {
                            Log.w(TAG, "Timed out waiting for pre-capture sequence to complete.");
                            readyToCapture = true;
                        }

                        if (readyToCapture && mPendingUserCaptures > 0) {
                            // Capture once for each user tap of the "Picture" button.
                            while (mPendingUserCaptures > 0) {
                                captureStillPictureLocked();
                                mPendingUserCaptures--;
                            }
                            // After this, the camera will go back to the normal state of preview.
                            mState = STATE_PREVIEW;
                        }
                    }
                }
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            process(result);
        }

    };

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles the still JPEG capture
     * request.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                     long timestamp, long frameNumber) {
            setUpFile(true);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            // Look up the ImageSaverBuilder for this request and update it with the CaptureResult
            synchronized (getCameraStateLock()) {
                finishedCaptureLocked();
            }

            toastFile();
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureFailure failure) {
            synchronized (getCameraStateLock()) {
                finishedCaptureLocked();
            }
            toast("Capture failed!");
        }

    };

    /**
     * Sets up state related to camera that is needed before opening a {@link CameraDevice}.
     */
    private void setUpCameraOutputs() {
        try {
            // Configure state.
            CameraCharacteristics characteristics
                    = AppUtilsKt.getCameraManager().getCameraCharacteristics(getDevice().getId());

//            // We only use a camera that supports RAW in this sample.
//            if (!contains(characteristics.get(
//                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),
//                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
////                continue;
//            }

            synchronized (getCameraStateLock()) {
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
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
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
                            synchronized (getCameraStateLock()) {
                                // The camera is already closed
                                if (null == getCameraDevice()) {
                                    return;
                                }

                                try {
                                    setup3AControlsLocked(mPreviewRequestBuilder);
                                    // Finally, we start displaying the camera preview.
                                    cameraCaptureSession.setRepeatingRequest(
                                            mPreviewRequestBuilder.build(),
                                            mPreCaptureCallback, getBackgroundHandler());
                                    mState = STATE_PREVIEW;
                                } catch (CameraAccessException | IllegalStateException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                // When the session is ready, we start displaying the preview.
                                mCaptureSession = cameraCaptureSession;
                            }
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
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
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
     * Initiate a still image capture.
     * <p/>
     * This function sends a capture request that initiates a pre-capture sequence in our state
     * machine that waits for auto-focus to finish, ending in a "locked" state where the lens is no
     * longer moving, waits for auto-exposure to choose a good exposure value, and waits for
     * auto-white-balance to converge.
     */
    private void takePicture() {
        synchronized (getCameraStateLock()) {
            mPendingUserCaptures++;

            // If we already triggered a pre-capture sequence, or are in a state where we cannot
            // do this, return immediately.
            if (mState != STATE_PREVIEW) {
                return;
            }

            try {
                // Trigger an auto-focus run if camera is capable. If the camera is already focused,
                // this should do nothing.
                if (!mNoAFRun) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                            CameraMetadata.CONTROL_AF_TRIGGER_START);
                }

                // If this is not a legacy device, we can also trigger an auto-exposure metering
                // run.
                if (!isLegacyLocked()) {
                    // Tell the camera to lock focus.
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                            CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                }

                // Update state machine to wait for auto-focus, auto-exposure, and
                // auto-white-balance (aka. "3A") to converge.
                mState = STATE_WAITING_FOR_3A_CONVERGENCE;

                // Start a timer for the pre-capture sequence.
                startTimerLocked();

                // Replace the existing repeating request with one with updated 3A triggers.
                mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback,
                        getBackgroundHandler());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a capture request to the camera device that initiates a capture targeting the JPEG
     * outputs.
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
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

            // Set request tag to easily track results in callbacks.
            captureBuilder.setTag(mRequestCounter.getAndIncrement());

            CaptureRequest request = captureBuilder.build();

            mCaptureSession.capture(request, mCaptureCallback, getBackgroundHandler());

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after a JPEG capture has completed; resets the AF trigger state for the
     * pre-capture sequence.
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
     */
    private void finishedCaptureLocked() {
        try {
            // Reset the auto-focus trigger in case AF didn't run quickly enough.
            if (!mNoAFRun) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

                mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback,
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
        synchronized (getCameraStateLock()) {
            // Increment reference count to prevent ImageReader from being closed while we
            // are saving its Images in a background thread (otherwise their resources may
            // be freed while we are writing to a file).
            if (mImageReader == null) {
                Log.e(TAG, "Paused the activity before we could save the image," +
                        " ImageReader already closed.");
                return;
            }

            final Image image;
            try {
                image = mImageReader.acquireNextImage();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Too many images queued for saving, dropping image.");
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
                            closeOutput(output);
                        }
                    } else {
                        Log.e(TAG, "Cannot save image, unexpected image format:" + format);
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
                                        Log.i(TAG, "Scanned " + path + ":");
                                        Log.i(TAG, "-> uri=" + uri);
                                    }
                                });
                    }
                }
            });
        }
    }

    // Utility classes and methods:
    // *********************************************************************************************

    /**
     * Cleanup the given {@link OutputStream}.
     *
     * @param outputStream the stream to close.
     */
    private static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    /**
     * Check if we are using a device that only supports the LEGACY hardware level.
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
     *
     * @return true if this is a legacy device.
     */
    private boolean isLegacyLocked() {
        return mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    /**
     * Start the timer for the pre-capture sequence.
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
     */
    private void startTimerLocked() {
        mCaptureTimer = SystemClock.elapsedRealtime();
    }

    /**
     * Check if the timer for the pre-capture sequence has been hit.
     * <p/>
     * Call this only with {@link #getCameraStateLock()} held.
     *
     * @return true if the timeout occurred.
     */
    private boolean hitTimeoutLocked() {
        return (SystemClock.elapsedRealtime() - mCaptureTimer) > PRECAPTURE_TIMEOUT_MS;
    }

}
