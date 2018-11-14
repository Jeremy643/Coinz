package com.coinz.jeremy.coinz;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class DownloadFileTask extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... urls) {
        try {
            //return loadFileFromNetwork(urls[0]);
            String fileDownload = loadFileFromNetwork(urls[0]);

            try {
                Writer output = null;
                File file = new File("data/data/com.coinz.jeremy.coinz/files/coinzmap.geojson");
                output = new BufferedWriter(new FileWriter(file));
                output.write(fileDownload);
                output.close();
            } catch (Exception e) {
                //Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            return fileDownload;
        } catch (IOException e) {
            return "Unable to load content. Check your network connection";
        }
    }

    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadUrl(new URL(urlString)));
    }

    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();
        return connection.getInputStream();
    }

    @NonNull
    private String readStream(InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream),1000);
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            stringBuilder.append(line);
        }
        stream.close();
        return stringBuilder.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        DownloadCompleteRunner.downloadComplete(result);
    }
}
