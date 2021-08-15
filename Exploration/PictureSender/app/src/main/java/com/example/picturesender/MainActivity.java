package com.example.picturesender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    //button to be pressed
    private Button mButton0;
    //For showing camera preview
    private SurfaceView mCameraView;
    //For setting up camera after a short delay
    private Handler mMainThreadHandler;
    //Need 2nd thread to send things to server
    private HandlerThread mNetworkThread;
    private Handler mNetworkThreadHandler;
    //Need 3rd thread to operate camera
    private CameraThread mCameraThread;
    //Context is needed for setting up camera thread
    private Context mContext;
    //A buffer to store a raw YUV picture
    //The content will be fetched from camera thread.
    private byte[] mRawImage = new byte[640 * 480 * 3 / 2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start network thread
        mNetworkThread = new HandlerThread("Network Thread");
        mNetworkThread.start();
        //needed for assigning network related tasks
        mNetworkThreadHandler = new Handler(mNetworkThread.getLooper());
        //Prepare a main thread handler, needed for delayed camera thread setting up
        mContext = this;
        //needed for setting up camera thread after a fraction of a second
        mMainThreadHandler = new Handler(getMainLooper());
        //Hook up with button and camera preview window in UI
        mButton0 = (Button) findViewById(R.id.button0);
        //Camera preview
        mCameraView = (SurfaceView) findViewById(R.id.surfaceView);
        //Determine what to do if the button is clicked.
        mButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ask network thread to send content in its thread, not in main thread
                if (mCameraThread != null) {

                    //copies the latest image to buffer from camera thread
                    mCameraThread.copyOutCurrentImage(mRawImage);
                    //creates yuv image object from the buffer
                    YuvImage yuvImage = new YuvImage(mRawImage, ImageFormat.NV21, 640, 480, null);
                    //prepares a storage buffer for compressed jpeg bytes
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    //jpeg compression
                    yuvImage.compressToJpeg(new Rect(0, 0, 640, 480), 90, buffer);
                    //acquires the compressed content in the form of byte array
                    byte[] jpegData = buffer.toByteArray();

                    //assign task to networkThread to send out jpeg file
                    mNetworkThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            sendPic(jpegData);
                        }
                    });
                }
            }
        });

        //Set up camera thread after .1s of delay
        mMainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCameraThread = new CameraThread(mContext, mCameraView.getHolder().getSurface());
            }
        }, 100);
    }

    //The function of sending content over the network
    //Note: This function is executed in network thread
    private void sendPic(final byte[] inbytes) {
        String host = new String("192.168.2.132");
        int port = 32421;
        //server address basically
        try {
            //Send file info 1st, file name and file length
            Socket socket = new Socket(host, port);
            OutputStream outputStream = socket.getOutputStream();
            String fileInfo = "test.jpeg " + inbytes.length;
            outputStream.write(fileInfo.getBytes());
            socket.close();

            //Send the file content
            socket = new Socket(host, port);
            outputStream = socket.getOutputStream();
            outputStream.write(inbytes);
            socket.close();
        } catch (Exception e) {
            //In case something is wrong, such as no internet, etc.
            e.printStackTrace();
        }
    }
}