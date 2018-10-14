package com.example.nikoleta.coinz;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, LocationEngineListener,
        PermissionsListener {
    private String tag = "MainActivity";
    private MapView mapView;
    static MapboxMap map;
    private LocationEngine locationEngine;
    private Location originLocation;


    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date date = new Date();
    String downloadDate = dateFormat.format(date); // Format: YYYY/MM/DD
    String url = "http://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson";

    private final String preferencesFile = "MyPrefsFile"; // for storing preferences

    static com.mapbox.mapboxsdk.annotations.Icon icon_quid;
    static com.mapbox.mapboxsdk.annotations.Icon icon_penny;
    static com.mapbox.mapboxsdk.annotations.Icon icon_dollar;
    static com.mapbox.mapboxsdk.annotations.Icon icon_shilling;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
            DownloadFileTask task = new DownloadFileTask();
            task.execute(url);
            IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
            BitmapDrawable iconDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.quid, null);
            assert iconDrawable != null;
            Bitmap bitmap = iconDrawable.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            icon_quid = iconFactory.fromBitmap(smallMarker);

            iconDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.dollar, null);
            assert iconDrawable != null;
            bitmap = iconDrawable.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            icon_dollar = iconFactory.fromBitmap(smallMarker);

            iconDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.penny, null);
            assert iconDrawable != null;
            bitmap = iconDrawable.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            icon_penny = iconFactory.fromBitmap(smallMarker);

            iconDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.shilling, null);
            assert iconDrawable != null;
            bitmap = iconDrawable.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            icon_shilling = iconFactory.fromBitmap(smallMarker);

            task.getStatus();
        }
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
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }
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
        mapView.onStart();
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onResume() {
        super.onResume();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");

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
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
