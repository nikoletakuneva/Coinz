package com.example.nikoleta.coinz;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import java.util.List;

public class DownloadCompleteRunner {
    private static String result;

    public static void downloadComplete(String result) {
        DownloadCompleteRunner.result = result;
        String geoJsonString = result;
        Feature feature;
        FeatureCollection fc = FeatureCollection.fromJson(geoJsonString);
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