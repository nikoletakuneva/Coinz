package com.example.nikoleta.coinz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SelectCoinGiftsActivity extends AppCompatActivity {
    static List<Coin> coins = new ArrayList<>();
    static List<Feature> features_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_coin_gifts);

        String walletString = "";
        if (coins.isEmpty()) {
            try {
                FileInputStream fis = openFileInput("wallet.geojson");
                walletString = MapActivity.readStream(fis);
                JSONObject jsonObject = new JSONObject(walletString);

                if (!jsonObject.get("features").toString().equals("[]")) {
                    FeatureCollection fc = FeatureCollection.fromJson(walletString);

                    features_list = fc.features();

                    assert features_list != null;
                    for (int i = 0; i < features_list.size(); i++) {
                        Feature feature = features_list.get(i);
                        Geometry g = feature.geometry();
                        assert g != null;
                        if (g.type().equals("Point")) {
                            JsonObject j = feature.properties();
                            assert j != null;
                            String currency = j.get("currency").toString().replaceAll("\"", "");
                            String id = j.get("id").toString().replaceAll("\"", "");
                            String valueStr = j.get("value").toString().replaceAll("\"", "");
                            double value = Double.parseDouble(valueStr);
                            Coin coin = new Coin(currency, value, id);
                            coins.add(coin);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        List<Coin> coinsSelected = new ArrayList<>();
        List<Feature> featuresSelected = new ArrayList<>();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), coins);
        gridview.setAdapter(adapter);
        TextView walletSummary = (TextView) findViewById(R.id.coins);
        int coinsNum = coins.size();
        double totalMoney = 0;
        for (Coin coin : coins) {
            String currency = coin.getCurrency();
            double rate = 0;
            switch (currency) {
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
            totalMoney = totalMoney + coin.getValue() * rate;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        walletSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum, totalMoney));

        Button sendGift = (Button) findViewById(R.id.send_coins_gift);
        sendGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coins.removeAll(coinsSelected);
                adapter.notifyDataSetChanged();
                features_list.removeAll(featuresSelected);
                adapter.selectedPositions.clear();

                Query searchUsers = db.collection("users").whereEqualTo("username", SendCoinsActivity.selectedUser);
                Task<QuerySnapshot> task = searchUsers.get();
                task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> taskFindUser) {
                        List<DocumentSnapshot> documentsList = taskFindUser.getResult().getDocuments();
                        if (documentsList.isEmpty()) {
                            Toast.makeText(SelectCoinGiftsActivity.this, "No such user.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            List<String> gifts = new ArrayList<>();
                            List<String> notifications = new ArrayList<>();

                            for (Coin c: coinsSelected) {
                                String coin = c.getValue() + " " + c.getCurrency() + " "  + c.getId();
                                gifts.add(coin);
                            }

                            for (DocumentSnapshot document : documentsList) {
                                DocumentReference docRef = db.collection("users").document(document.getId());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskGifts) {
                                        int coinsLeft;
                                        if (taskGifts.getResult().contains("gifts")) {
                                            // Remove the square brackets from the String of previous gifts stored in the database and split it into coins
                                            String[] prevGifts = taskGifts.getResult().get("gifts").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");

                                            if (taskGifts.getResult().contains("notifications")) {
                                                String[] prevNotifications = taskGifts.getResult().get("notifications").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                                                for (String n: prevNotifications) {
                                                    notifications.add(n);
                                                }
                                            }
                                            for (String gift: prevGifts) {
                                                gifts.add(gift);
                                            }
                                        }
                                        db.collection("users").document(document.getId()).update("gifts", gifts);

                                        DocumentReference docRefUsername = db.collection("users").document(user.getUid());
                                        docRefUsername.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskGetUsername) {
                                                if (taskGetUsername.isSuccessful()) {
                                                    DocumentSnapshot documentCurrUser = taskGetUsername.getResult();
                                                    if (documentCurrUser.exists()) {
                                                        String username = documentCurrUser.getString("username");
                                                        String notification = username + " sent you a gift.";
                                                        notifications.add(notification);

                                                        db.collection("users").document(document.getId()).update("notifications", notifications);
                                                    }
                                                    else {
                                                        Log.d("SelectCoinGiftsActivity", "No such document");
                                                    }
                                                }
                                                else {
                                                    Log.d("SelectCoinGiftsActivity", "get failed with ", taskGetUsername.getException());
                                                }
                                            }
                                        });
                                    }
                                });
                            }

                            coinsSelected.clear();
                            featuresSelected.clear();

                            int coinsNum = coins.size();
                            double totalMoney = 0;
                            for (Coin coin : coins) {
                                String currency = coin.getCurrency();
                                double rate = 0;
                                switch (currency) {
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
                                totalMoney = totalMoney + coin.getValue() * rate;
                            }

                            walletSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum, totalMoney));
                            updateWalletFile();

                        }
                    }
                });


            }
        });

        Button selectAll = (Button) findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coinsSelected.size() != coinsNum) {
                    adapter.selectedPositions.clear();
                    coinsSelected.clear();
                    coinsSelected.addAll(coins);
                    featuresSelected.clear();
                    featuresSelected.addAll(features_list);
                    for (int i = 0; i < coinsNum; i++) {
                        adapter.selectedPositions.add(i);
                        View viewItem = gridview.getChildAt(i);
                        if (viewItem != null) {
                            viewItem.setBackgroundResource(R.drawable.coin_selected);
                        }
                    }
                }
            }
        });

        Button deselectAll = (Button) findViewById(R.id.deselect_all);
        deselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coinsSelected.size() != 0) {
                    adapter.selectedPositions.clear();
                    coinsSelected.clear();
                    featuresSelected.clear();
                    for (int i = 0; i < coinsNum; i++) {
                        View viewItem = gridview.getChildAt(i);
                        if (viewItem != null) {
                            viewItem.setBackgroundResource(R.drawable.coin_not_selected);
                        }

                    }
                }

            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter.selectedPositions.indexOf(position);
                if (selectedIndex > -1) {
                    adapter.selectedPositions.remove(selectedIndex);
                    v.setBackgroundResource(R.drawable.coin_not_selected);
                    Coin c = ImageAdapter.coins.get(position);
                    coinsSelected.remove(c);
                    featuresSelected.remove(c);
                } else {
                    adapter.selectedPositions.add(position);
                    v.setBackgroundResource(R.drawable.coin_selected);
                    Coin c = ImageAdapter.coins.get(position);
                    coinsSelected.add(c);
                    featuresSelected.add(features_list.get(position));
                }
            }
        });
    }

    private void updateWalletFile() {
        FeatureCollection fcWallet = FeatureCollection.fromFeatures(features_list);
        String geoJsonWallet = fcWallet.toJson();
        DownloadCompleteRunner.writeFile(geoJsonWallet, "wallet.geojson");
    }
}
