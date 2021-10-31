package com.apps4si.TwitterSeller;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FinalActivity extends AppCompatActivity {

    private TextView bigText;
    private String id;
    private Button homeButton;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        bigText = findViewById(R.id.textViewCompleted);
        homeButton = findViewById(R.id.homeButton);

        Intent intent = getIntent();
        id = intent.getStringExtra(MainActivity.LISTING_ID);

        String website = "https://apps4sj.org/" + id;
        String text = "Your listing has been posted! View your listing at <a href='" + website + "'> " + website + "</a>";
        bigText.setGravity(Gravity.CENTER);
        bigText.setClickable(true);
        bigText.setMovementMethod(LinkMovementMethod.getInstance());
        bigText.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinalActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}