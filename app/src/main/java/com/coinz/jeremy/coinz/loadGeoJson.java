package com.coinz.jeremy.coinz;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;

public class loadGeoJson {

    private static String tag = "loadGeoJson";

    private static String path = "/data/data/com.coinz.jeremy.coinz/files/coinzmap.geojson";

    static JsonObject jsonObject;
    private static File file = new File(path);
    private static JsonParser parser = new JsonParser();
    static MapboxMap mapbox;

    static HashMap<String, JsonArray> markersOnMap = new HashMap<>();

    public static void displayMarkers(MapboxMap mapboxMap) {
        try {
            mapbox = mapboxMap;

            Object obj = parser.parse(new FileReader(file));

            jsonObject = (JsonObject) obj;
            Log.d(tag, "[displayMarkers] jsonObject: " + jsonObject);

            JsonArray jsonArrayFeatures = jsonObject.getAsJsonArray("features");
            Log.d(tag, "[displayMarkers] jsonArrayFeatures size: " + jsonArrayFeatures.size());

            for (int i = 0; i < jsonArrayFeatures.size(); i++) {
                JsonObject feature = (JsonObject) jsonArrayFeatures.get(i);

                //Get the marker's coordinates.
                JsonObject geometry = (JsonObject) feature.get("geometry");
                JsonArray coordinates = (JsonArray) geometry.get("coordinates");

                double lng = coordinates.get(0).getAsDouble();
                double lat = coordinates.get(1).getAsDouble();

                JsonObject properties = (JsonObject) feature.get("properties");
                String markerSymbol = properties.get("marker-symbol").getAsString();
                String markerCurrency = properties.get("currency").getAsString();

                //Display markers.
                mapboxMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).setTitle(markerSymbol).setSnippet(markerCurrency));

                //Get each marker's unique id.
                String markerID = properties.get("id").getAsString();

                //Save the id and coordinates in a HashMap.
                markersOnMap.put(markerID, coordinates);
                Log.d(tag, "[displayMarkers] markersOnMap: " + markersOnMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeMarkerFromFile(String closestID) {
        try {
            Object obj = parser.parse(new FileReader(file));

            jsonObject = (JsonObject) obj;

            JsonArray jsonArrayFeatures = jsonObject.getAsJsonArray("features");

            for (int i = 0; i < jsonArrayFeatures.size(); i++) {
                JsonObject feature = (JsonObject) jsonArrayFeatures.get(i);
                JsonObject properties = (JsonObject) feature.get("properties");

                //Get each marker's unique id.
                String markerID = properties.get("id").getAsString();

                if (closestID.equals(markerID)) {
                    jsonArrayFeatures.remove(i);
                    break;
                }
            }
            Log.d(tag, "[removeMarkerFromFile] jsonArrayFeatures size: " + jsonArrayFeatures.size());

            JsonObject jObject = new JsonObject();
            jObject.add("features", jsonArrayFeatures);
            obj = jsonObject;

            //Save changed file.
            Writer output = null;
            File file = new File(path);
            output = new BufferedWriter(new FileWriter(file));
            output.write(String.valueOf(obj));
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getRates() {
        //Get the exchange rate.
        JsonObject jsonObjectRates = new JsonObject();

        try {
            Object object = parser.parse(new FileReader(file));

            jsonObject = (JsonObject) object;
            Log.d(tag, "[getRates] jsonObject: " + jsonObject);

            jsonObjectRates = (JsonObject) jsonObject.get("rates");
            Log.d(tag, "[getRates] jsonArrayRates: " + jsonObjectRates);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObjectRates;
    }
}
