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
    Button mButton0;
    //For showing camera preview
    SurfaceView mCameraView;
    //For setting up camera after a short delay
    Handler mMainThreadHandler;
    //Need 2nd thread to send things to server
    HandlerThread mNetworkThread;
    Handler mNetworkThreadHandler;
    //Need 3rd thread to operate camera
    CameraThread mCameraThread;
    //Context is needed for setting up camera thread
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start network thread
        mNetworkThread = new HandlerThread("Network Thread");
        mNetworkThread.start();
        mNetworkThreadHandler = new Handler(mNetworkThread.getLooper());
        //Prepare a main thread handler, needed for delayed camera thread setting up
        mContext = this;
        mMainThreadHandler = new Handler(getMainLooper());
        //Hook up with button and camera preview window in UI
        mButton0 = (Button) findViewById(R.id.button0);
        mCameraView = (SurfaceView) findViewById(R.id.surfaceView);
        //Determine what to do if the button is clicked.
        mButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ask network thread to send content in its thread, not in main thread
                mNetworkThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Pretend the following byte array includes a jpeg image.
                        byte[] jpegImage = {1, 2, 3, 4, 5, 6, 7, 8, 9};
                        sendPic(jpegImage);
                    }
                });
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
        String host = new String("192.168.2.139");
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
        } catch (Exception e) {
            //In case something is wrong, such as no internet, etc.
            e.printStackTrace();
        }
    }
}