package com.apps4sj.TwitterSeller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class DeleteActivity extends AppCompatActivity {

    private Button deleteButton, goBackButton;
    private EditText idInput;
    private TextView listingsTextView;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        goBackButton = findViewById(R.id.buttonGoBack);
        deleteButton = findViewById(R.id.buttonDelete);
        idInput = findViewById(R.id.editTextID);
        listingsTextView = findViewById(R.id.textViewListings);

        updateText();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.sql.idExists(idInput.getText().toString())) {
                    Toast.makeText(DeleteActivity.this, "ID does not exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(DeleteActivity.this, "Deleted " + idInput.getText().toString(), Toast.LENGTH_SHORT).show();
                MainActivity.sql.deleteListing(idInput.getText().toString());

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(MainActivity.HOST, MainActivity.PORT);
                            OutputStream outputStream = socket.getOutputStream();

                            JSONObject toSend = new JSONObject();
                            toSend.put("type", "delete");
                            toSend.put("id", idInput.getText().toString());

                            int headerNum = 45; //toSend.toString().getBytes().length;
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

                updateText();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeleteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateText() {
        List<Listing> listings = MainActivity.sql.getAllListings();

        String allListings = "";
        for (Listing l : listings) {
            allListings += l + "\n";
        }
        listingsTextView.setText(allListings);
    }
}