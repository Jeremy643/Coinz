package com.coinz.jeremy.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map;

public class depositPopupActivity extends Activity {

    private final String tag = "depositPopupActivity";

    private Button closeDepositPopup;
    private Button confirmDeposit;

    private final String preferencesFile = "MyPrefsFile";
    private final String markerInfoFile = "MyMarkerFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_popup);

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
        ListView listView = findViewById(R.id.coinsList);
        listView.setAdapter(adapter);

        ArrayList<String> items = new ArrayList<>();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!view.isEnabled()) {
                    //Remove the item previously selected and enable it to be selected again.
                    items.remove(String.valueOf(listView.getItemAtPosition(position)));
                    Log.d(tag, "[onCreate] items size: " + items.size());
                    view.setEnabled(true);
                } else {
                    //Stores items selected by the user.
                    items.add(String.valueOf(listView.getItemAtPosition(position)));
                    Log.d(tag, "[onCreate] items size: " + items.size());

                    //Once item is selected, remove the ability to choose it again.
                    view.setEnabled(false);
                }
            }
        });
        Log.d(tag, "[onCreate] items size: " + items.size());

        closeDepositPopup = findViewById(R.id.closeDepositPopup);
        closeDepositPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SharedPreferences settingsPref = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPref = settingsPref.edit();
        int depositLimit = Integer.valueOf(settingsPref.getString("depositLimit", "0"));
        confirmDeposit = findViewById(R.id.confirmDeposit);
        confirmDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When clicked, the selected items will be converted to gold and removed from the list.
                if (!((depositLimit + items.size()) > 25)) {
                    //Perform the deposit.
                    double goldVal = Double.valueOf(settingsPref.getString("goldValue", "0"));
                    //Get the exchange rate.
                    JsonObject rates = loadGeoJson.getRates();
                    double shilRate = rates.get("SHIL").getAsDouble();
                    double dolrRate = rates.get("DOLR").getAsDouble();
                    double quidRate = rates.get("QUID").getAsDouble();
                    double penyRate = rates.get("PENY").getAsDouble();

                    for (int i = 0; i < items.size(); i++) {
                        //Retrieve the info on each selected item.
                        String[] itemInfo = items.get(i).split(", ");

                        String itemID = itemInfo[0].split(": ")[1];
                        String itemCurrency = itemInfo[1].split(": ")[1];
                        String itemVal = itemInfo[2].split(": ")[1];

                        //Update the gold value and the values held in the wallet.
                        switch (itemCurrency) {
                            case "SHIL":
                                goldVal += (Double.valueOf(itemVal) * shilRate);
                                double shilVal = Double.valueOf(settingsPref.getString("shilValue","0"));
                                shilVal -= Double.valueOf(itemVal);
                                editorPref.putString("shilValue", String.valueOf(shilVal));
                                break;
                            case "DOLR":
                                goldVal += (Double.valueOf(itemVal) * dolrRate);
                                double dolrVal = Double.valueOf(settingsPref.getString("dolrValue","0"));
                                dolrVal -= Double.valueOf(itemVal);
                                editorPref.putString("dolrValue", String.valueOf(dolrVal));
                                break;
                            case "QUID":
                                goldVal += (Double.valueOf(itemVal) * quidRate);
                                double quidVal = Double.valueOf(settingsPref.getString("quidValue","0"));
                                quidVal -= Double.valueOf(itemVal);
                                editorPref.putString("quidValue", String.valueOf(quidVal));
                                break;
                            case "PENY":
                                goldVal += (Double.valueOf(itemVal) * penyRate);
                                double penyVal = Double.valueOf(settingsPref.getString("penyValue","0"));
                                penyVal -= Double.valueOf(itemVal);
                                editorPref.putString("penyValue", String.valueOf(penyVal));
                                break;
                        }
                        editorPref.putString("goldValue", String.valueOf(goldVal));
                        //Update the total value that is held in the wallet.
                        double totVal = Double.valueOf(settingsPref.getString("totalValue", "0"));
                        totVal -= Double.valueOf(itemVal);
                        editorPref.putString("totalValue", String.valueOf(totVal));
                        editorPref.apply();

                        //Remove selected coin from the collected coins file.
                        SharedPreferences.Editor editor = settings.edit();
                        editor.remove(itemID);
                        editor.apply();
                    }

                    int newDepositLimit = depositLimit + items.size();
                    editorPref.putString("depositLimit", String.valueOf(newDepositLimit));
                    editorPref.apply();
                }
                finish();
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