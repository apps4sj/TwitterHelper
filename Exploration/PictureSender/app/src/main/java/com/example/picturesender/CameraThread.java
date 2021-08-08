package com.example.picturesender;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Range;
import android.view.Surface;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CameraThread extends HandlerThread {
    private ImageReader mImageReader;
    private Context mContext;
    private Handler mThreadHandler;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private Surface mPreviewSurface;
    private Surface mImageReaderSurface;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private static final String[] VIDEO_PERMISSIONS = {
            "android.permission.CAMERA"
    };
    //callback for image reader
    ImageReader.OnImageAvailableListener mOnImageAvailableListener=new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null){
                //Do something about the image
                image.close();
            }
        }
    };

    //Callbacks in camera open
    private CameraDevice.StateCallback mCameraOpenStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startVideoCapture();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
            Toast.makeText(mContext, "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    };

    private void startVideoCapture() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(mPreviewSurface);
            mCaptureRequestBuilder.addTarget(mImageReaderSurface);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(mPreviewSurface);
            outputSurfaces.add(mImageReaderSurface);
            mCameraDevice.createCaptureSession(outputSurfaces,
                    mCreateCaptureSessionStateCallback, mThreadHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(mContext, "Failed to start capturing.", Toast.LENGTH_SHORT).show();
        }
    }

    //Callbacks in setup capture session
    private CameraCaptureSession.StateCallback mCreateCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            mCameraCaptureSession = cameraCaptureSession;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Toast.makeText(mContext, "Failed to start capture session.", Toast.LENGTH_SHORT).show();
        }
    };

    protected void updatePreview() {
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mThreadHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public CameraThread(Context context, Surface inSurface) {
        super("CameraThread");
        mContext = context;
        mPreviewSurface = inSurface;
        //Check if permission is already granted in setup
        boolean isPermittedInSetup = true;
        for (String permission : VIDEO_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                isPermittedInSetup = false;
            }
        }
        if (!isPermittedInSetup) {
            Toast.makeText(mContext, "Please go to settings to grant permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        //Thread start
        start();
        //Get thread handler
        mThreadHandler = new Handler(getLooper());
        //Set up an ImageReader to access each camera frame, 30 times per second
        mImageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888, 3);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mThreadHandler);
        mImageReaderSurface = mImageReader.getSurface();
        //Get CameraManager
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            //String cameraId = mCameraManager.getCameraIdList()[0];
            String cameraId = "0";
            mCameraManager.openCamera(cameraId, mCameraOpenStateCallback, mThreadHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(mContext, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
        }
    }
}
