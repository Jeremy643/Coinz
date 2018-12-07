package com.coinz.jeremy.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;

public class walletPopupActivity extends Activity {

    private static String tag = "walletPopupActivity";

    private Button walletCloseButton;
    private TextView shilValue;
    private TextView dolrValue;
    private TextView quidValue;
    private TextView penyValue;
    private TextView totalValue;

    static double shilVal;
    static double dolrVal;
    static double quidVal;
    static double penyVal;
    static double totalVal;

    private static final String preferencesFile = "MyPrefsFile";

    @Override
    @SuppressWarnings("ConstantConditions") //Removes the warning of loading a null from sharedprefs.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_popup);

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        //Making sure that the necessary values are being stored in sharedprefs.
        if (!settings.contains("shilValue")) {
            //SharedPreferences.Editor editor = settings.edit();
            editor.putString("shilValue", "0");
            editor.apply();
        }
        if (!settings.contains("dolrValue")) {
            //SharedPreferences.Editor editor = settings.edit();
            editor.putString("dolrValue", "0");
            editor.apply();
        }
        if (!settings.contains("quidValue")) {
            //SharedPreferences.Editor editor = settings.edit();
            editor.putString("quidValue", "0");
            editor.apply();
        }
        if (!settings.contains("penyValue")) {
            //SharedPreferences.Editor editor = settings.edit();
            editor.putString("penyValue", "0");
            editor.apply();
        }
        if (!settings.contains("totalValue")) {
            //SharedPreferences.Editor editor = settings.edit();
            editor.putString("totalValue", "0");
            editor.apply();
        }

        walletCloseButton = findViewById(R.id.walletCloseButton);
        walletCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Load the previous contents of the wallet.
        shilVal = Double.valueOf(settings.getString("shilValue", "0"));
        Log.d(tag, "[onCreate] shilVal: " + shilVal);

        dolrVal = Double.valueOf(settings.getString("dolrValue", "0"));
        Log.d(tag, "[onCreate] dolrVal: " + dolrVal);

        quidVal = Double.valueOf(settings.getString("quidValue", "0"));
        Log.d(tag, "[onCreate] quidVal: " + quidVal);

        penyVal = Double.valueOf(settings.getString("penyValue", "0"));
        Log.d(tag, "[onCreate] penyVal: " + penyVal);

        totalVal = Double.valueOf(settings.getString("totalValue", "0"));
        Log.d(tag, "[onCreate] totalVal: " + totalVal);

        //Display these values in their corresponding TextView.
        shilValue = findViewById(R.id.shilValue);
        shilValue.setText(Double.toString(shilVal));
        shilValue.setTextColor(Color.BLACK);

        dolrValue = findViewById(R.id.dolrValue);
        dolrValue.setText(Double.toString(dolrVal));
        dolrValue.setTextColor(Color.BLACK);

        quidValue = findViewById(R.id.quidValue);
        quidValue.setText(Double.toString(quidVal));
        quidValue.setTextColor(Color.BLACK);

        penyValue = findViewById(R.id.penyValue);
        penyValue.setText(Double.toString(penyVal));
        penyValue.setTextColor(Color.BLACK);

        totalValue = findViewById(R.id.totalValue);
        totalValue.setText(Double.toString(totalVal));
        totalValue.setTextColor(Color.BLACK);

        //Define the layout of the popup window.
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
