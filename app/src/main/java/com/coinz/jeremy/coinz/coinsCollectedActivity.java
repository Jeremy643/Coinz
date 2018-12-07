package com.coinz.jeremy.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Map;

public class coinsCollectedActivity extends AppCompatActivity {

    private Button closeCoinsCollected;

    private final String markerInfoFile = "MyMarkerFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coins_collected);

        SharedPreferences settings = getSharedPreferences(markerInfoFile, Context.MODE_PRIVATE);

        String[] markerInfo = new String[settings.getAll().size()];
        //Get the keys for the file.
        Map<String, ?> contentsFile = settings.getAll();
        String[] keys = contentsFile.keySet().toArray(new String[contentsFile.size()]);
        for (int i = 0; i < settings.getAll().size(); i++) {
            String key = keys[i];
            markerInfo[i] = settings.getString(key, "N/A");
        }

        //Displaying all of the collected coins in the listView.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, markerInfo);
        ListView listView = findViewById(R.id.coinsCollectedList);
        listView.setAdapter(adapter);

        closeCoinsCollected = findViewById(R.id.closeCoinsCollected);
        closeCoinsCollected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
