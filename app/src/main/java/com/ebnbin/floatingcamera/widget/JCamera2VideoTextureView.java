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

import android.Manifest;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;
import com.ebnbin.floatingcamera.util.CameraHelper;
import com.ebnbin.floatingcamera.util.PermissionHelper;
import com.ebnbin.floatingcamera.util.PreferenceHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class JCamera2VideoTextureView extends CameraView {

    private CameraManager mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    private WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

    private void init() {
        mTextureView = this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera();

            record();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();

        closeCamera();
        stopBackgroundThread();

        super.onDetachedFromWindow();
    }

    private void onClick() {
        if (mIsRecordingVideo) {
            stopRecordingVideo();
        } else {
            startRecordingVideo();
        }
    }

    private void record() {
        runOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsRecordingVideo) {
                    return;
                }

                onClick();
            }
        });
    }

    private void stop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsRecordingVideo) {
                    return;
                }

                onClick();
            }
        });
    }

    private boolean isFinishing() {
        return !isAttachedToWindow();
    }

    private void finish() {
        if (isFinishing()) {
            return;
        }

        mWindowManager.removeView(this);
    }

    private void runOnUiThread(final Runnable action) {
        post(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                action.run();
            }
        });
    }

    private void runOnUiThreadDelayed(final Runnable action) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                action.run();
            }
        }, 1000L);
    }

    private void toast(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void error(String message) {
        toast(message);
        Log.e("ebnbin", message);
    }

    //*****************************************************************************************************************

    public JCamera2VideoTextureView(Context context) {
        this(context, null);

        init();
    }

    public JCamera2VideoTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        init();
    }

    public JCamera2VideoTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    //*****************************************************************************************************************

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    private static final String TAG = "JCamera2VideoTextureVie";
    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * An {@link JCamera2VideoTextureView} for camera preview.
     */
    private /*AutoFitTextureView*/ JCamera2VideoTextureView mTextureView;

    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera();

            record();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
//            Activity activity = getActivity();
//            if (null != activity) {
                /*activity.*/finish();
//            }
        }

    };
    private Integer mSensorOrientation;
    private File mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressWarnings("MissingPermission")
    private void openCamera() {
        if (PermissionHelper.INSTANCE.isPermissionsDenied(Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO)) {
            finish();
            return;
        }

        if (/*null == activity || activity.*/isFinishing()) {
            return;
        }
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            String cameraId = PreferenceHelper.INSTANCE.device().getId2();

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = /*manager*/mCameraManager.getCameraCharacteristics(cameraId);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            mVideoSize = PreferenceHelper.INSTANCE.resolution().getSize();

            configureTransform();
            mMediaRecorder = new MediaRecorder();
            /*manager*/mCameraManager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
//            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            toast("Cannot access the camera.");
            /*activity.*/finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
//            ErrorDialog.newInstance(/*getString(R.string.camera_error)*/"This device doesn't support Camera2 API.")
//                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            error("This device doesn't support Camera2 API.");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        CameraHelper.Device.Resolution previewResolution = getPreviewResolution();

        if (null == mCameraDevice || !mTextureView.isAvailable() || null == previewResolution) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewResolution.getWidth(), previewResolution.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
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
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void setUpMediaRecorder() throws IOException {
        if (/*null == activity*/isFinishing()) {
            return;
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null) {
            mNextVideoAbsolutePath = getVideoFilePath();
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath.getAbsolutePath());
//        mMediaRecorder.setVideoEncodingBitRate(10000000);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = /*activity.getWindowManager()*/mWindowManager.getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }

        CameraHelper.Device.VideoProfile videoProfile = PreferenceHelper.INSTANCE.videoProfile();
        if (videoProfile == null) {
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//            if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
//                mNextVideoAbsolutePath = getVideoFilePath(/*getActivity()*/getContext());
//            }
//            mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//            int rotation = /*activity.getWindowManager()*/mWindowManager.getDefaultDisplay().getRotation();
//            switch (mSensorOrientation) {
//                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
//                    mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
//                    break;
//                case SENSOR_ORIENTATION_INVERSE_DEGREES:
//                    mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
//                    break;
//            }
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

        if (null == mCameraDevice || !mTextureView.isAvailable() || null == previewResolution) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewResolution.getWidth(), previewResolution.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
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
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

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
            }, mBackgroundHandler);
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
        if (/*null != activity*/!isFinishing()) {
//            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
//                    Toast.LENGTH_SHORT).show();
            toast("Video saved: " + mNextVideoAbsolutePath);
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        mNextVideoAbsolutePath = null;
//        startPreview();
    }

}
