package com.coinz.jeremy.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class collectCoins extends Activity {

    private static String tag = "collectCoins";

    private static HashMap<String, JsonArray> markersOnMap = loadGeoJson.markersOnMap;
    private static String closestID;
    private static String lastClosestID;
    private static JsonObject jsonObject = loadGeoJson.jsonObject;
    private static Context mContext;

    private final String preferencesFile = "MyPrefsFile";
    private final String markerInfoFile = "MyMarkerFile";

    public collectCoins(){}

    public collectCoins(Context context) {
        mContext = context;
    }

    public static boolean nextTo(Location location) {
        Location markersLocation = new Location("");
        String[] keys = markersOnMap.keySet().toArray(new String[markersOnMap.size()]);

        //minDist is initialised to an arbitrary value.
        float minDist = 1000;
        int index = 0;

        for (int i = 0; i < markersOnMap.size(); i++) {
            JsonArray coordinates = markersOnMap.get(keys[i]);

            double lng = coordinates.get(0).getAsDouble();
            markersLocation.setLongitude(lng);
            double lat = coordinates.get(1).getAsDouble();
            markersLocation.setLatitude(lat);

            if (location.distanceTo(markersLocation) < minDist) {
                minDist = location.distanceTo(markersLocation);
                index = i;
            }
        }

        if (minDist <= 25 && minDist >= 0) {
            if (lastClosestID == null) {
                lastClosestID = "";
            }
            closestID = keys[index];
            if (closestID.equals(lastClosestID)) {
                MainActivity.justAsked = true;
                Log.d(tag, "[nextTo] The user is within 25m of a coin they decided not to collect. Marker index: " + closestID);
                return true;
            } else {
                lastClosestID = closestID;
                MainActivity.justAsked = false;
                Log.d(tag, "[nextTo] The user is within 25m of a coin they have not see before. Marker index: " + closestID);
                return true;
            }
        } else {
            Log.d(tag, "[nextTo] There is no coin within 25m of the user.");
            return false;
        }
    }

    public static double getMarkerValue() {
        JsonArray jsonArrayFeatures = jsonObject.getAsJsonArray("features");
        Log.d(tag, "[getMarkerValue] jsonArrayFeatures size: " + jsonArrayFeatures.size());

        double markerValue = 0;
        for (int i = 0; i < jsonArrayFeatures.size(); i++) {
            JsonObject feature = (JsonObject) jsonArrayFeatures.get(i);

            JsonObject properties = (JsonObject) feature.get("properties");
            String coinID = properties.get("id").getAsString();

            if (coinID.equals(closestID)) {
                markerValue = properties.get("value").getAsDouble();
                break;
            }
        }

        return markerValue;
    }

    @SuppressWarnings("ConstantConditions")
    public void removeCoins() {
        markersOnMap.remove(closestID);
        Log.d(tag, "[removeCoins] markersOnMap: " + markersOnMap);

        JsonArray jsonArrayFeatures = jsonObject.getAsJsonArray("features");
        Log.d(tag, "[getMarkerValue] jsonArrayFeatures size: " + jsonArrayFeatures.size());

        //Get the key details of the closest marker.
        String markerValue = "";
        String markerCurrency = "";
        for (int i = 0; i < jsonArrayFeatures.size(); i++) {
            JsonObject feature = (JsonObject) jsonArrayFeatures.get(i);

            JsonObject properties = (JsonObject) feature.get("properties");
            String coinID = properties.get("id").getAsString();

            if (coinID.equals(closestID)) {
                markerValue = properties.get("value").getAsString();
                markerCurrency = properties.get("currency").getAsString();
                break;
            }
        }

        SharedPreferences settings = mContext.getSharedPreferences(markerInfoFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        //Save the marker info to our marker info file.
        String markerInfo = "id: " + closestID + ", currency: " + markerCurrency + ", value: " + markerValue;
        editor.putString(closestID, markerInfo);
        editor.apply();

        SharedPreferences settingsPrefs = mContext.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPrefs = settingsPrefs.edit();
        switch (markerCurrency) {
            case "SHIL":
                double shilVal = Double.valueOf(settingsPrefs.getString("shilValue", "0"));
                shilVal += Double.valueOf(markerValue);
                editorPrefs.putString("shilValue", String.valueOf(shilVal));
                break;
            case "DOLR":
                double dolrVal = Double.valueOf(settingsPrefs.getString("dolrValue", "0"));
                dolrVal += Double.valueOf(markerValue);
                editorPrefs.putString("dolrValue", String.valueOf(dolrVal));
                break;
            case "QUID":
                double quidVal = Double.valueOf(settingsPrefs.getString("quidValue", "0"));
                quidVal += Double.valueOf(markerValue);
                editorPrefs.putString("quidValue", String.valueOf(quidVal));
                break;
            case "PENY":
                double penyVal = Double.valueOf(settingsPrefs.getString("penyValue", "0"));
                penyVal += Double.valueOf(markerValue);
                editorPrefs.putString("penyValue", String.valueOf(penyVal));
                break;
        }

        double total = Double.valueOf(settingsPrefs.getString("totalValue", "0"));
        total += Double.valueOf(markerValue);
        editorPrefs.putString("totalValue", String.valueOf(total));
        editorPrefs.apply();

        //Clear map of markers.
        loadGeoJson.mapbox.clear();

        //Update coinzmap file to remove closest marker.
        loadGeoJson.removeMarkerFromFile(closestID);
        //After updating the file, re-display the remaining coins.
        loadGeoJson.displayMarkers(loadGeoJson.mapbox);
    }
}
