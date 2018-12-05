package com.example.nikoleta.coinz;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.ArrayList;
import java.util.Arrays;
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

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject rates = jsonObject.getJSONObject("rates");
            MapActivity.rateDOLR = rates.getDouble("DOLR");
            MapActivity.ratePENY = rates.getDouble("PENY");
            MapActivity.rateQUID = rates.getDouble("QUID");
            MapActivity.rateSHIL = rates.getDouble("SHIL");

            MapActivity.quid_rate.setText(String.format("%.2f GOLD", MapActivity.rateQUID));
            MapActivity.dolr_rate.setText(String.format("%.2f GOLD", MapActivity.rateDOLR));
            MapActivity.peny_rate.setText(String.format("%.2f GOLD", MapActivity.ratePENY));
            MapActivity.shil_rate.setText(String.format("%.2f GOLD", MapActivity.rateSHIL));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (MapActivity.fileDownloaded == 0){
            writeFile(result, "coinzmap.geojson");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();

            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> taskWallet) {
                    if (taskWallet.getResult().contains("wallet")) {
                        String[] coins = taskWallet.getResult().get("wallet").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                        if (!coins[0].equals("")) {
                            double maxValue = 0.0;
                            double maxCoinValue = 0.0;
                            String maxCurrency = "";
                            String maxId = "";
                            for (String c : coins) {
                                if (!c.equals("")) {
                                    String[] coinProperties = c.split(" ");
                                    double rate = 0;
                                    switch (coinProperties[1]) {
                                        case "QUID":
                                            rate = MapActivity.rateQUID;
                                            break;
                                        case "DOLR":
                                            rate = MapActivity.rateDOLR;
                                            break;
                                        case "PENY":
                                            rate = MapActivity.ratePENY;
                                            break;
                                        case "SHIL":
                                            rate = MapActivity.rateSHIL;
                                            break;
                                    }
                                    double value = Double.parseDouble(coinProperties[0]) * rate;
                                    if (value > maxValue) {
                                        maxValue = value;
                                        maxCoinValue =  Double.parseDouble(coinProperties[0]);
                                        maxCurrency = coinProperties[1];
                                        maxId = coinProperties[2];
                                    }
                                }
                            }
                            String maxCoin = String.valueOf(maxCoinValue) + " " + maxCurrency + " " + maxId;
                            List<String> piggybankCoinsList = new ArrayList<>();
                            piggybankCoinsList.add(maxCoin);
                            if (taskWallet.getResult().contains("piggybank")) {
                                // Remove the square brackets from the String of previous gifts stored in the database and split it into coins
                                String[] prevGifts = taskWallet.getResult().get("piggybank").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                                for (String c: prevGifts) {
                                    if (!c.equals("")){
                                        piggybankCoinsList.add(c);
                                    }
                                }
                            }
                            db.collection("users").document(user.getUid()).update("piggybank", piggybankCoinsList);
                        }
                    }
                    db.collection("users").document(user.getUid()).update("wallet", new ArrayList<String>());
                    db.collection("users").document(user.getUid()).update("coinsLeft", 25);
                    db.collection("users").document(user.getUid()).update("magnetUnlocked", false);
                    db.collection("users").document(user.getUid()).update("stealUnlocked", false);
                    db.collection("users").document(user.getUid()).update("shieldUnlocked", false);
                    db.collection("users").document(user.getUid()).update("magnetMode", false);
                    db.collection("users").document(user.getUid()).update("stealUsed", false);
                }
            });
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