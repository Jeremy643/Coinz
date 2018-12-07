package com.coinz.jeremy.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class bankPopupActivity extends Activity {

    private Button bankCloseButton;
    private Button bankCoinsButton;
    private Button exchangeRateButton;
    private TextView goldCoinsDisplay;
    private TextView numberDeposits;

    private final String preferencesFile = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_popup);

        if (MainActivity.stopped) {
            //Giving the user bonus gold coins depending on the level they achieved.
            SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            String level = settings.getString("level", "Novice");

            double goldVal = Double.valueOf(settings.getString("goldValue", "0"));
            switch (level) {
                case "Expert":
                    goldVal += 100;
                    break;
                case "Advanced":
                    goldVal += 75;
                    break;
                case "Intermediate":
                    goldVal += 50;
                    break;
                case "Beginner":
                    goldVal += 25;
                    break;
                case "Novice":
                    goldVal += 0;
                    break;
            }

            editor.putString("goldValue", String.valueOf(goldVal));
            editor.apply();

            MainActivity.stopped = false;
        }

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (!settings.contains("goldValue")) {
            editor.putString("goldValue", "0");
            editor.apply();
        }

        String goldCoins = settings.getString("goldValue", "0");
        goldCoinsDisplay = findViewById(R.id.goldCoinsDisplay);
        goldCoinsDisplay.setText(goldCoins);

        bankCloseButton = findViewById(R.id.bankCloseButton);
        bankCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bankCoinsButton = findViewById(R.id.bankCoinsButton);
        bankCoinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), depositPopupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        exchangeRateButton = findViewById(R.id.exchangeRateButton);
        exchangeRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), exchangeRatePopupActivity.class);
                startActivity(intent);
            }
        });

        String depositLimit = settings.getString("depositLimit", "0");
        numberDeposits = findViewById(R.id.numberDeposits);
        numberDeposits.setText(depositLimit);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.8));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }
}
