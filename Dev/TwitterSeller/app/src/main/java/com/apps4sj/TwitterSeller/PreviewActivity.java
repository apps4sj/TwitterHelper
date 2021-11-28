package com.apps4sj.TwitterSeller;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;

public class PreviewActivity extends AppCompatActivity {

    private ImageView previewImage;
    private Button yesButton, noButton;

    private String id;
    private Socket socket;

    private String saveInstance;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        previewImage = findViewById(R.id.previewImageView);
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);

        Intent intent = getIntent();

        byte[] previewArray = intent.getByteArrayExtra(MainActivity.PREVIEW_IMAGE_BYTES);
        Bitmap preview = BitmapFactory.decodeByteArray(previewArray, 0, previewArray.length);
//        Bitmap previewBitmap = BitmapFactory.decodeFile(previewPath);
        previewImage.setImageBitmap(preview);

        id = intent.getStringExtra(MainActivity.LISTING_ID);
        saveInstance = intent.getStringExtra(MainActivity.SAVE_INSTANCE);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(MainActivity.HOST, MainActivity.PORT);
                            OutputStream outputStream = socket.getOutputStream();

                            JSONObject toSend = new JSONObject();
                            toSend.put("type", "publish");
                            toSend.put("id", id);

                            int headerNum = 46; //toSend.toString().getBytes().length;
                            StringBuilder header = new StringBuilder(String.valueOf(headerNum));
                            while (header.length() < 10) {
                                header.insert(0, "0");
                            }
                            System.out.println(header);
                            System.out.println(toSend.toString());

                            String endJson = "\n";

                            outputStream.write(header.toString().getBytes());
                            outputStream.write(toSend.toString().getBytes());
                            outputStream.write(endJson.getBytes());

                            System.out.println("published");

                            socket.close();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(PreviewActivity.this, FinalActivity.class);
                                    intent.putExtra(MainActivity.LISTING_ID, id);
                                    startActivity(intent);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.sql.deleteListing(id);

                Intent intent = new Intent(PreviewActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.SAVE_INSTANCE, saveInstance);
                startActivity(intent);
            }
        });

    }
}
