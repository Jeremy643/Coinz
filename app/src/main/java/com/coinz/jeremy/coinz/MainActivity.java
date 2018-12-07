package com.coinz.jeremy.coinz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonArray;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private FloatingActionButton walletButton;
    private FloatingActionButton bankButton;
    private TextView markerCounter;
    private TextView countdownDisplay;
    private TextView levelsDisplay;

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

    String todayDate = Integer.toString(year) + "/" + sMonth + "/" + sDay;
    private String downloadDate;
    String lastDownloadDate;

    private boolean firstMarker;
    private boolean lastMarker;
    private CountDownTimer countDownTimer;
    private long timeLeftInMilliseconds = 7200000; //2 hours.
    static boolean stopped = false;

    private final String preferencesFile = "MyPrefsFile";
    private final String markerInfoFile = "MyMarkerFile";

    private String url = "http://homepages.inf.ed.ac.uk/stg/coinz/" + year + "/" + sMonth + "/" + sDay + "/coinzmap.geojson";

    static boolean justAsked = false;

    private FirebaseAuth mAuth;

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
        Log.d(tag, "[onCreate] Recalled lastDownloadDate is '" + lastDownloadDate + "'");

        if (!lastDownloadDate.equals(todayDate)) {
            new DownloadFileTask().execute(url);
            downloadDate = todayDate;

            markerCounter = findViewById(R.id.markerCounter);
            markerCounter.setText("50");

            countdownDisplay = findViewById(R.id.countdownDisplay);
            countdownDisplay.setText("02:00:00");
            firstMarker = true;

            //Stops the coinzmap from re-downloading everytime the user opens the app on a new day without
            //ending the app.
            Log.d(tag, "[onCreate] Storing lastDownloadDate of " + downloadDate);
            settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastDownloadDate", downloadDate);
            editor.putString("markerCounter", "50");
            editor.putString("depositLimit", "0");
            editor.putString("firstMarker", String.valueOf(firstMarker));
            editor.apply();
        } else {
            downloadDate = todayDate;
            /*settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("depositLimit", "25");
            editor.apply();*/
        }

        invalidateOptionsMenu();
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
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0));
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);

            if (collectCoins.nextTo(location) && !justAsked) {
                //Collect coin.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Collect coin?");

                CharSequence message = "You are within 25m of a coin. Do you want to collect it?";
                builder.setMessage(message);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
                        //Loads the total amount stored in the wallet.
                        double totalVal = Double.valueOf(settings.getString("totalValue", "0"));
                        //"markerVal" gets the value of the marker the user is collecting.
                        double markerVal = collectCoins.getMarkerValue();

                        //Check the amount of space left in the wallet.
                        if ((totalVal + markerVal) > 150) {
                            //Display a warning to the user that they can't collect the coin.
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            builder.setTitle("Wallet full!");

                            CharSequence message = "You do not have any more room left in your wallet. Deposit coins in your bank!";
                            builder.setMessage(message);

                            builder.setPositiveButton("Bank", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Take the user to the bank.
                                    Intent intent = new Intent(getApplicationContext(), bankPopupActivity.class);
                                    startActivity(intent);
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //How do I close both dialogs from here? Is it even a problem?
                                    dialog.cancel();
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } else {
                            firstMarker = Boolean.valueOf(settings.getString("firstMarker", "false"));
                            if (firstMarker) {
                                firstMarker = false;
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("firstMarker", String.valueOf(firstMarker));
                                editor.apply();

                                //Starts the timer.
                                startTimer();

                                //Calls "removeCoins" in "collectCoins" to collect the nearest coin.
                                collectCoins collect = new collectCoins(getApplicationContext());
                                collect.removeCoins();
                                //Update the counter on screen so they can see how many coins are left.
                                updateMarkerCounter();
                                //Cancel the alert.
                                dialog.cancel();
                            } else {
                                //If the user collects the last coin then we want to stop the countdown.
                                if (loadGeoJson.markersOnMap.size() == 1) {
                                    lastMarker = true;
                                    //Calls "removeCoins" in "collectCoins" to collect the nearest coin.
                                    collectCoins collect = new collectCoins(getApplicationContext());
                                    collect.removeCoins();
                                    //Update the counter on screen so they can see how many coins are left.
                                    updateMarkerCounter();

                                    //Stop the timer.
                                    stopTimer();

                                    //Cancel the alert.
                                    dialog.cancel();
                                } else {
                                    //Calls "removeCoins" in "collectCoins" to collect the nearest coin.
                                    collectCoins collect = new collectCoins(getApplicationContext());
                                    collect.removeCoins();
                                    //Update the counter on screen so they can see how many coins are left.
                                    updateMarkerCounter();
                                    //Cancel the alert.
                                    dialog.cancel();
                                }
                            }
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        justAsked = true;
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    public void stopTimer() {
        countDownTimer.cancel();
        stopped = true;
    }

    public void updateTimer() {
        int hours = (int) timeLeftInMilliseconds / 3600000;
        int minutes = (int) timeLeftInMilliseconds % 3600000 / 60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;

        String timeLeftText = "";

        if (hours < 10) {
            timeLeftText += "0";
        }
        timeLeftText += hours;

        timeLeftText += ":";
        if (minutes < 10) {
            timeLeftText += "0";
        }
        timeLeftText += minutes;

        timeLeftText += ":";
        if (seconds < 10) {
            timeLeftText += "0";
        }
        timeLeftText += seconds;

        levelsDisplay = findViewById(R.id.levelsDisplay);

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        int timeLeft = (hours * 60) + minutes + (seconds / 60);
        if (timeLeft >= 90) {
            levelsDisplay.setText("Expert");
            editor.putString("level", "Expert");
            editor.apply();
        } else {
            if (timeLeft >= 60) {
                levelsDisplay.setText("Advanced");
                editor.putString("level", "Advanced");
                editor.apply();
            } else {
                if (timeLeft >= 30) {
                    levelsDisplay.setText("Intermediate");
                    editor.putString("level", "Intermediate");
                    editor.apply();
                } else {
                    if (timeLeft > 0) {
                        levelsDisplay.setText("Beginner");
                        editor.putString("level", "Beginner");
                        editor.apply();
                    } else {
                        levelsDisplay.setText("Novice");
                        editor.putString("level", "Novice");
                        editor.apply();
                    }
                }
            }
        }

        countdownDisplay = findViewById(R.id.countdownDisplay);
        countdownDisplay.setText(timeLeftText);
    }

    public void updateMarkerCounter() {
        int markerNum = loadGeoJson.markersOnMap.size();

        markerCounter = findViewById(R.id.markerCounter);

        markerCounter.setText(String.valueOf(markerNum));
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Explanation on why the user needs to grant permission for their location.
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        boolean logged = Boolean.valueOf(settings.getString("loggedIn", "false"));
        if (logged) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            String email = user.getEmail();
            menu.findItem(R.id.loginButton).setTitle(email);
        } else {
            menu.findItem(R.id.loginButton).setTitle("Login");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.introductionCoinz) {
            //Takes the user to a window that explains the app.
            Intent intent = new Intent(getApplicationContext(), WelcometoCoinz.class);
            startActivity(intent);

            return true;
        }

        if (id == R.id.coinsCollected) {
            //Open window that displays, as a list, all the coins the user has collected.
            Intent intent = new Intent(getApplicationContext(), coinsCollectedActivity.class);
            startActivity(intent);

            return true;
        }

        if (id == R.id.emptyWallet) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Empty wallet");

            CharSequence message = "Are you sure? If you continue, you will not be able to get your coins back!";
            builder.setMessage(message);

            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences settingsPref = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorPref = settingsPref.edit();

                    editorPref.putString("dolrValue", "0");
                    editorPref.putString("shilValue", "0");
                    editorPref.putString("quidValue", "0");
                    editorPref.putString("penyValue", "0");
                    editorPref.putString("totalValue", "0");
                    editorPref.apply();

                    SharedPreferences settings = getSharedPreferences(markerInfoFile, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.clear();
                    editor.apply();

                    dialog.cancel();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        }

        if (id == R.id.emptyBank) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Empty bank");

            CharSequence message = "Are you sure? If you continue, you will not be able to get your coins back!";
            builder.setMessage(message);

            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Clear the coins from sharedprefs.
                    SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.putString("goldValue", "0");
                    editor.putString("depositLimit", "0");
                    editor.apply();

                    dialog.cancel();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        }

        if (id == R.id.shareCoins) {
            return true;
        }

        if (id == R.id.loginButton) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

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

        String markerNum = settings.getString("markerCounter", "N/A");
        Log.d(tag, "[onStart] markerCounter: " + markerNum);

        firstMarker = Boolean.getBoolean(settings.getString("firstMarker", "false"));
        Log.d(tag, "[onStart] firstMarker: " + firstMarker);

        markerCounter = findViewById(R.id.markerCounter);
        markerCounter.setText(markerNum);

        levelsDisplay = findViewById(R.id.levelsDisplay);
        String level = settings.getString("levels", "Novice");
        levelsDisplay.setText(level);

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

        //Storing the marker counter value in sharedprefs.
        markerCounter = findViewById(R.id.markerCounter);
        CharSequence markerNum = markerCounter.getText();
        editor.putString("markerCounter", String.valueOf(markerNum));

        editor.putString("firstMarker", "false");
        countdownDisplay = findViewById(R.id.countdownDisplay);
        countdownDisplay.setText("02:00:00");

        if (!lastMarker) {
            editor.putString("levels", "Novice");
            levelsDisplay.setText("Novice");
        }

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