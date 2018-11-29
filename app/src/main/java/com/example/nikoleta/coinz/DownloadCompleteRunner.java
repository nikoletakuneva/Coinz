package com.example.nikoleta.coinz;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class DownloadCompleteRunner {
    private static String result;
    static String ratesStr = "";

    public static void writeFile(String str, String fileName) {
        // Add the geojson text into a file in internal storage
        try {
            FileOutputStream file = getApplicationContext().openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(file);
            outputWriter.write(str);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadComplete(String result) {
        DownloadCompleteRunner.result = result;
        int ind = result.indexOf("features");
        ratesStr = result.substring(0,ind-1);
        Feature feature;
        FeatureCollection fc = FeatureCollection.fromJson(result);

        if (MapActivity.fileDownloaded == 0){
            writeFile(result, "coinzmap.geojson");
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject rates = jsonObject.getJSONObject("rates");
            MapActivity.rateDOLR = rates.getDouble("DOLR");
            MapActivity.ratePENY = rates.getDouble("PENY");
            MapActivity.rateQUID = rates.getDouble("QUID");
            MapActivity.rateSHIL = rates.getDouble("SHIL");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Feature> features_list = fc.features();

        assert features_list != null;
        for (int i = 0; i < features_list.size(); i++) {
            feature = features_list.get(i);
            Geometry g = feature.geometry();
            assert g != null;
            if (g.type().equals("Point")) {
                Point point = (Point) g;
                List<Double> coordinates = point.coordinates();
                LatLng latlng = new LatLng(coordinates.get(1), coordinates.get(0));
                JsonObject j = feature.properties();
                assert j != null;
                String currency = j.get("currency").toString().replaceAll("\"", "");
                String id = j.get("id").toString().replaceAll("\"", "");
                String value = j.get("value").toString().replaceAll("\"", "");
                com.mapbox.mapboxsdk.annotations.Icon icon = null;

                switch (currency){
                    case "QUID": icon = MapActivity.icon_quid;
                        break;
                    case "DOLR": icon = MapActivity.icon_dollar;
                        break;
                    case "PENY": icon = MapActivity.icon_penny;
                        break;
                    case "SHIL": icon = MapActivity.icon_shilling;
                        break;
                }

                MapActivity.map.addMarker(new MarkerOptions().title(String.format("%.2f", Double.parseDouble(value)) + " " + currency).snippet(id).icon(icon).position(latlng));
            }

        }

    }
}