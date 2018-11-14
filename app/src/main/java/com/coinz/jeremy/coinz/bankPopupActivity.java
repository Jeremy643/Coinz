package com.coinz.jeremy.coinz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class bankPopupActivity extends Activity {

    private Button bankCloseButton;
    private Button bankCoinsButton;
    private Button exchangeRateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_popup);

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
                //We want a screen to appear that allows the user to deposit coins into their bank.
                //The user should be able to select the currency(-ies) and the amount to be
                //deposited, as long as they have coins in the currencies they chose and they don't
                //exceed the deposit limit.
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
