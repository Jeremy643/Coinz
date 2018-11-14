package com.coinz.jeremy.coinz;

import android.location.Location;
import android.util.Log;

import com.google.gson.JsonArray;

import java.util.HashMap;

public class collectCoins {

    private static String tag = "collectCoins";

    private static HashMap<String, JsonArray> markersOnMap = loadGeoJson.markersOnMap;
    private static int keyToClosest;

    public static boolean nextTo(Location location) {
        JsonArray coordinates;
        Location markersLocation = new Location("");
        String[] keys = markersOnMap.keySet().toArray(new String[markersOnMap.size()]);

        //minDist is initialised to an arbitrary value.
        float minDist = 1000;
        int index = 0;

        for (int i = 0; i < markersOnMap.size(); i++) {
            coordinates = markersOnMap.get(keys[i]);

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
            keyToClosest = index;
            Log.d(tag, "[nextTo] There is a coin within 25m of the user. Marker index: " + keyToClosest);
            return true;
        } else {
            Log.d(tag, "[nextTo] There is no coin within 25m of the user.");
            return false;
        }
    }
}
