package com.coinz.jeremy.coinz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private FloatingActionButton walletButton;
    private FloatingActionButton bankButton;

    private String tag = "MainActivity";

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    private Calendar cal = Calendar.getInstance();
    private DecimalFormat decimalFormat = new DecimalFormat("00");
    private double day = cal.get(Calendar.DAY_OF_MONTH);
    private String sDay = decimalFormat.format(day);
    private double month = cal.get(Calendar.MONTH)+1;
    private String sMonth = decimalFormat.format(month);
    private int year = cal.get(Calendar.YEAR);

    private String todayDate = Integer.toString(year) + "/" + sMonth + "/" + sDay;
    private String downloadDate;
    private String lastDownloadDate;

    private final String preferencesFile = "MyPrefsFile";

    private String url = "http://homepages.inf.ed.ac.uk/stg/coinz/" + year + "/" + sMonth + "/" + sDay + "/coinzmap.geojson";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        walletButton = findViewById(R.id.walletButton);
        walletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), walletPopupActivity.class);
                startActivity(intent);
            }
        });

        bankButton = findViewById(R.id.bankButton);
        bankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), bankPopupActivity.class);
                startActivity(intent);
            }
        });

        //We load the "lastDownloadDate" from the shared preferences file.
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        lastDownloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '" + lastDownloadDate + "'");

        if (!lastDownloadDate.equals(todayDate)) {
            new DownloadFileTask().execute(url);
            downloadDate = todayDate;

            //Stops the coinzmap from re-downloading everytime the user opens the app on a new day without
            //ending the app.
            Log.d(tag, "[onCreate] Storing lastDownloadDate of " + downloadDate);
            settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastDownloadDate", downloadDate);
            editor.apply();
        } else {
            downloadDate = todayDate;
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;

            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            enableLocation();

            //We call the method "displayMarkers" in our "loadGeoJson" class to display the coins on the map.
            loadGeoJson.displayMarkers(mapboxMap);
        }
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "[enableLocation] Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "[enableLocation] Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();

        locationEngine.addLocationEngineListener(this);

        locationEngine.setInterval(5000);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        //locationEngine.requestLocationUpdates();

        Location lastLocation = locationEngine.getLastLocation();
        Log.d(tag, "[initializeLocationEngine] lastLocation: " + lastLocation);
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null");
        } else {
            if (map == null) {
                Log.d(tag, "map is null");
            } else {
                locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0)); //13.0
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);

            if (collectCoins.nextTo(location)) {
                //Collect coin.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Collect coin?");

                CharSequence message = "You are within 25m of a coin.";
                builder.setMessage(message);

                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Explanation on why the user needs to grant permission for their location.
        /*Log.d(tag, "Permissions: " + permissionsToExplain.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Note");

        CharSequence message = "To play the game you must allow access to your location.";
        builder.setMessage(message);

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();*/

        Context context = getApplicationContext();
        CharSequence text = "Allow access!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            //If the user does not grant access to their location then a dialog box will appear displaying a warning message.
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Warning!");

            CharSequence message = "You have not granted access to your location.";
            builder.setMessage(message);

            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        lastDownloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '" + lastDownloadDate + "'");

        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        editor.apply();

        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }
}
