package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationEngineListener,
        PermissionsListener {

    private String tag = "MainActivity";
    private MapView mapView;
    static MapboxMap map;
    private LocationEngine locationEngine;
    private Location originLocation;
    private boolean mapChange = false;

    String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences

    static com.mapbox.mapboxsdk.annotations.Icon icon_quid;
    static com.mapbox.mapboxsdk.annotations.Icon icon_penny;
    static com.mapbox.mapboxsdk.annotations.Icon icon_dollar;
    static com.mapbox.mapboxsdk.annotations.Icon icon_shilling;

    static double rateDOLR, rateQUID, ratePENY, rateSHIL;

    static int fileDownloaded = 0;

    static List<Coin> walletCoins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currUser = firebaseAuth.getCurrentUser();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
         ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView email = headerView.findViewById(R.id.nav_header_email);
        email.setText(currUser.getEmail());

        TextView usernameText = headerView.findViewById(R.id.usernameView);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(currUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("ProfileScreen", "DocumentSnapshot data: " + document.getData());
                        String username = document.getString("username");
                        usernameText.setText(username);
                    } else {
                        Log.d("ProfileScreen", "No such document");
                    }
                } else {
                    Log.d("ProfileScreen", "get failed with ", task.getException());
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        Mapbox.getInstance(this,
                "pk.eyJ1Ijoibmlrb2xldGFrdW5ldmEiLCJhIjoiY2puNHAzNTcwMDFxbjNxbzRpbzZyN2s3ZSJ9.AE8kZVzoWo1uOZs4JXogDA");
        mapView = findViewById(R.id.mapboxMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            // Make location information available
            enableLocation();

            //Set the icons for each currency
            icon_dollar = getIcon(R.drawable.dollar);
            icon_penny = getIcon(R.drawable.penny);
            icon_quid = getIcon(R.drawable.quid);
            icon_shilling = getIcon(R.drawable.shilling);

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            String todayDate = dateFormat.format(date);

            if (!todayDate.equals(downloadDate)) {
                //Download the GeoJSON file
                DownloadFileTask task = new DownloadFileTask();
                String url = "http://homepages.inf.ed.ac.uk/stg/coinz/" + todayDate + "/coinzmap.geojson";
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                db.collection("users").document(user.getUid()).update("wallet", new ArrayList<String>());
                db.collection("users").document(user.getUid()).update("coinsLeft", 25);
                downloadDate = todayDate;
                task.execute(url);
            }
            else {
                //Load map from the downloaded file
                String geoJsonString = "";
                fileDownloaded = 1;
                try {
                    FileInputStream fis = openFileInput("coinzmap.geojson");
                    geoJsonString = readStream(fis);
                    DownloadCompleteRunner.downloadComplete(geoJsonString);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @NonNull
    public static String readStream(InputStream stream)
            throws IOException {
        // Read input from stream, build result as a string
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(stream),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        stream.close();
        return sb.toString();
    }

    private Icon getIcon(int resource) {
        IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
        BitmapDrawable iconDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), resource, null);
        assert iconDrawable != null;
        Bitmap bitmap = iconDrawable.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        Icon icon = iconFactory.fromBitmap(smallMarker);
        return icon;
    }

    @SuppressLint("LogNotTimber")
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "Permissions are not granted");
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this)
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressLint("LogNotTimber")
    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null");
        } else {
            if (map == null) {
                Log.d(tag, "map is null");
            } else {
                LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView,
                        map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        }
        else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);

            double user_latitude = location.getLatitude();
            double user_longitude = location.getLongitude();

            for (Marker marker: map.getMarkers()) {
                LatLng latlng = marker.getPosition();
                double marker_latitude = latlng.getLatitude();
                double marker_longitude = latlng.getLongitude();
                if (distance (user_latitude, user_longitude, marker_latitude, marker_longitude) <= 25) {
                    mapChange = true;
                    map.removeMarker(marker);

                    String valueCurrency = marker.getTitle();
                    String id = marker.getSnippet();
                    String currency = valueCurrency.substring(valueCurrency.length() - 4);
                    String valueStr = valueCurrency.replace( " " + currency, "");
                    double value = Double.parseDouble(valueStr);
                    Coin coin = new Coin(currency, value, id);
                    walletCoins.add(coin);

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user  = firebaseAuth.getCurrentUser()  ;
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    List<String> walletCoinsList = new ArrayList<>();

                    String coinStr = coin.getValue() + " " + coin.getCurrency() + " "  + coin.getId();
                    walletCoinsList.add(coinStr);

                    DocumentReference docRef = db.collection("users").document(user.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult().contains("wallet")) {
                                String[] prevCoins = task.getResult().get("wallet").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                                walletCoinsList.addAll(Arrays.asList(prevCoins));
                            }
                            db.collection("users").document(user.getUid()).update("wallet", walletCoinsList);
                        }
                    });
                }
            }

        }
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371000; // in metres

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }

    @Override
    @SuppressWarnings("MissingPermission")
    @SuppressLint("LogNotTimber")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    @SuppressLint("LogNotTimber")
    public void onExplanationNeeded(List<String>
                                            permissionsToExplain){
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
        // Present toast or dialog.
    }

    @Override
    @SuppressLint("LogNotTimber")
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            // Open a dialogue with the user
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");

        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");

        mapView.onStart();
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onStop() {
        super.onStop();

        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        // Apply the edits!
        editor.apply();

        updateFile();

        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateFile();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
            finish();
        }
    }

    public void updateFile() {
        if (mapChange == true) {
            List<Feature> featuresList = new ArrayList<>();
            for (Marker marker : map.getMarkers()) {
                LatLng pos = marker.getPosition();
                Point p = Point.fromLngLat(pos.getLongitude(), pos.getLatitude());
                Geometry g = (Geometry) p;
                Feature f = Feature.fromGeometry(g);
                f.addStringProperty("id", marker.getSnippet());
                String valueCurrency = marker.getTitle();
                String currency = valueCurrency.substring(valueCurrency.length() - 4);
                String value = valueCurrency.replace(" " + currency, "");
                f.addStringProperty("currency", currency);
                f.addStringProperty("value", value);
                featuresList.add(f);
            }
            FeatureCollection fc = FeatureCollection.fromFeatures(featuresList);
            String geoJsonString = fc.toJson().substring(1);
            DownloadCompleteRunner.writeFile(DownloadCompleteRunner.ratesStr + geoJsonString, "coinzmap.geojson");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().contains("newNotifications")) {
                    if (task.getResult().getBoolean("newNotifications") == true) {
                        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
                        navigationView.getMenu().getItem(4).setIconTintMode(null).setIcon(R.drawable.notifications);
                    }
                }
            }
        });

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_wallet) {
            startActivity(new Intent(getApplicationContext(), Wallet.class));
        } else if (id == R.id.nav_piggybank) {
            startActivity(new Intent(getApplicationContext(), Piggybank.class));
        } else if (id == R.id.nav_bank) {
            startActivity(new Intent(getApplicationContext(), Bank.class));
        } else if (id == R.id.nav_boost) {
            startActivity(new Intent(getApplicationContext(), BoostersActivity.class));
        } else if (id == R.id.nav_gift) {
            startActivity(new Intent(getApplicationContext(), SelectUserActivity.class));
        }
        else if (id == R.id.nav_notifications) {
            NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
            navigationView.getMenu().getItem(4).setIconTintMode(null).setIcon(R.drawable.no_notifications);
            startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
        }
        else if (id == R.id.nav_logout) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();;
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
