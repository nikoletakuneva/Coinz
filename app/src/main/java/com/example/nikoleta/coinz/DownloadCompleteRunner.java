package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.List;
import java.util.Objects;
import static android.content.Context.MODE_PRIVATE;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class DownloadCompleteRunner {
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

    @SuppressLint("DefaultLocale")
    public static void downloadComplete(String result) {
        int ind = result.indexOf("features");
        ratesStr = result.substring(0,ind-1);
        Feature feature;
        FeatureCollection fc = FeatureCollection.fromJson(result);

        // Display the rates of the currencies on the map.
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


        // Check if this is the first time of the day that the app has been opened.
        if (MapActivity.fileDownloaded == 0){
            //Save map for the day in local storage.
            writeFile(result, "coinzmap.geojson");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();

            assert user != null;
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(taskWallet -> {
                // Find the coin with highest value in the wallet and transfer it to the piggybank.
                if (Objects.requireNonNull(taskWallet.getResult()).contains("wallet")) {
                    String[] coins = Objects.requireNonNull(taskWallet.getResult().get("wallet")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
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
                            String[] prevGifts = Objects.requireNonNull(taskWallet.getResult().get("piggybank")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
                            for (String c: prevGifts) {
                                if (!c.equals("")){
                                    piggybankCoinsList.add(c);
                                }
                            }
                        }
                        db.collection("users").document(user.getUid()).update("piggybank", piggybankCoinsList);
                    }
                }

                // Reset database fields for the day.
                db.collection("users").document(user.getUid()).update("wallet", new ArrayList<String>());
                db.collection("users").document(user.getUid()).update("coinsLeft", 25);
                db.collection("users").document(user.getUid()).update("magnetUnlocked", false);
                db.collection("users").document(user.getUid()).update("stealUnlocked", false);
                db.collection("users").document(user.getUid()).update("shieldUnlocked", false);
                db.collection("users").document(user.getUid()).update("magnetMode", false);
                db.collection("users").document(user.getUid()).update("stealUsed", false);
                db.collection("users").document(user.getUid()).update("piggybankProtected", false);
                db.collection("users").document(user.getUid()).update("cantStealFrom", new ArrayList<String>());
            });
        }

        // Add the markers to the map
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