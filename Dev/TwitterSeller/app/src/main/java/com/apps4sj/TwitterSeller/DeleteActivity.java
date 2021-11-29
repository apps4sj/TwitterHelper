package com.apps4sj.TwitterSeller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class DeleteActivity extends AppCompatActivity {

    private Button deleteButton, goBackButton;
    //private TextView listingsTextView;
    private ListView listView;
    private String currentID = "";
    private String[] listingList = {""};

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        goBackButton = findViewById(R.id.buttonGoBack);
        deleteButton = findViewById(R.id.buttonDelete);
        listView = findViewById(R.id.listView);
        deleteButton.setVisibility(View.INVISIBLE);
        updateText();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Extract ID from current listing
                String currentListing = listingList[i];
                int idPosition = currentListing.lastIndexOf("ID:");
                currentID = currentListing.substring(idPosition + 3);
                deleteButton.setVisibility(View.VISIBLE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.sql.idExists(currentID)) {
                    Toast.makeText(DeleteActivity.this, "ID does not exist", Toast.LENGTH_SHORT).show();
                    currentID = "";
                    deleteButton.setVisibility(View.INVISIBLE);
                    return;
                }
                deleteButton.setVisibility(View.INVISIBLE);
                Toast.makeText(DeleteActivity.this, "Deleted ID:" + currentID, Toast.LENGTH_SHORT).show();
                MainActivity.sql.deleteListing(currentID);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(MainActivity.HOST, MainActivity.PORT);
                            OutputStream outputStream = socket.getOutputStream();

                            JSONObject toSend = new JSONObject();
                            toSend.put("type", "delete");
                            toSend.put("id", currentID);

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
        listView.invalidateViews();
        List<Listing> listings = MainActivity.sql.getAllListings();
        listingList = new String[listings.size()];
        int i = 0;
        for (Listing l : listings) {
            listingList[i] = l.toString();
            i++;
        }

        ArrayAdapter<String> adsSummaryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listingList);
        listView.setAdapter(adsSummaryAdapter);
    }
}