package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DownloadCompleteRunner {
    static String result;
    static String geoJsonString;

    public static void downloadComplete(String result) {
        DownloadCompleteRunner.result = result;
        geoJsonString = result;
        Feature feature;
        FeatureCollection fc = FeatureCollection.fromJson(geoJsonString);
        List<Feature> features_list = fc.features();
        for (int i = 0; i < features_list.size(); i++) {
            feature = features_list.get(i);
            Geometry g = feature.geometry();
            String type = g.type();

            if (g.type().equals("Point")) {
                Point point = (Point) g;
                List<Double> coordinates = point.coordinates();
                LatLng latlng = new LatLng(coordinates.get(1), coordinates.get(0));
                JsonObject j = feature.properties();
                String currency = j.get("currency").toString();
                String id = j.get("id").toString();
                String value = j.get("value").toString();
                com.mapbox.mapboxsdk.annotations.Icon icon = null;



                switch (currency){
                    case "\"QUID\"": icon = MainActivity.icon_quid;
                        break;
                    case "\"DOLR\"": icon = MainActivity.icon_dollar;
                        break;
                    case "\"PENY\"": icon = MainActivity.icon_penny;
                        break;
                    case "\"SHIL\"": icon = MainActivity.icon_shilling;
                        break;
                }

                MainActivity.map.addMarker(new MarkerOptions().title(id).snippet(value).icon(icon).position(latlng));
            }

        }
    }
}