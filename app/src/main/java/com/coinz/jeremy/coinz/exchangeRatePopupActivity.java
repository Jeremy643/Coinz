package com.coinz.jeremy.coinz;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;

public class exchangeRatePopupActivity extends Activity {

    private Button closeExchangeRateButton;
    private TextView shilRateDisplay;
    private TextView dolrRateDisplay;
    private TextView quidRateDisplay;
    private TextView penyRateDisplay;

    private JsonObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate_popup);

        closeExchangeRateButton = findViewById(R.id.closeExchangeRateButton);
        closeExchangeRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        jsonObject = loadGeoJson.getRates();

        String shilVal = jsonObject.get("SHIL").getAsString();
        shilRateDisplay = findViewById(R.id.shilRateDisplay);
        shilRateDisplay.setText(shilVal);
        shilRateDisplay.setTextColor(Color.BLACK);

        String dolrVal = jsonObject.get("DOLR").getAsString();
        dolrRateDisplay = findViewById(R.id.dolrRateDisplay);
        dolrRateDisplay.setText(dolrVal);
        dolrRateDisplay.setTextColor(Color.BLACK);

        String quidVal = jsonObject.get("QUID").getAsString();
        quidRateDisplay = findViewById(R.id.quidRateDisplay);
        quidRateDisplay.setText(quidVal);
        quidRateDisplay.setTextColor(Color.BLACK);

        String penyVal = jsonObject.get("PENY").getAsString();
        penyRateDisplay = findViewById(R.id.penyRateDisplay);
        penyRateDisplay.setText(penyVal);
        penyRateDisplay.setTextColor(Color.BLACK);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.52));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }
}
