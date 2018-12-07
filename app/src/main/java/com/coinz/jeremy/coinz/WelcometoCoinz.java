package com.coinz.jeremy.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class WelcometoCoinz extends AppCompatActivity {

    private Button closeInfoScreen;
    private TextView informationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcometo_coinz);

        //Loading the text file from the assets folder and displaying it in the textView.
        String text = "";
        try {
            InputStream inputStream = getAssets().open("IntroductionToCoinz.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        informationText = findViewById(R.id.informationText);
        informationText.setText(text);

        closeInfoScreen = findViewById(R.id.closeInfoScreen);
        closeInfoScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close the window.
                finish();
            }
        });
    }
}
