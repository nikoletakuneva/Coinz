package com.example.nikoleta.coinz;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectCoinGiftsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_coin_gifts);

        if(MapActivity.walletCoins.isEmpty()) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user  = firebaseAuth.getCurrentUser()  ;
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            List<String> walletCoinsList = new ArrayList<>();

            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().contains("wallet")) {
                        String[] prevCoins = task.getResult().get("wallet").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                        if (!prevCoins[0].equals("")) {
                            for (String c: prevCoins) {
                                walletCoinsList.add(c);
                                String[] coinProperties = c.split(" ");
                                Coin coin = new Coin(coinProperties[1], Double.parseDouble(coinProperties[0]), coinProperties[2]);
                                MapActivity.walletCoins.add(coin);
                            }
                        }
                    }
                    walletView();
                }
            });
        }
        else {
            walletView();
        }
    }

    private void walletView() {
        List<Coin> coinsSelected = new ArrayList<>();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), MapActivity.walletCoins);
        gridview.setAdapter(adapter);
        TextView walletSummary = (TextView) findViewById(R.id.coins);
        int coinsNum = MapActivity.walletCoins.size();
        double totalMoney = 0;
        for (Coin coin : MapActivity.walletCoins) {
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
                DocumentReference docRef = db.collection("users").document(user.getUid());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(Double.parseDouble(task.getResult().get("coinsLeft").toString()) == 0) {
                            MapActivity.walletCoins.removeAll(coinsSelected);
                            adapter.notifyDataSetChanged();
                            adapter.selectedPositions.clear();

                            Query searchUsers = db.collection("users").whereEqualTo("username", SelectUserActivity.selectedUser);
                            Task<QuerySnapshot> taskSearch = searchUsers.get();
                            taskSearch.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                        List<String> users = new ArrayList<>();

                                        double giftsSum=0;
                                        for (Coin c: coinsSelected) {
                                            giftsSum = giftsSum + c.getGOLDValue();
                                            String coin = c.getValue() + " " + c.getCurrency() + " "  + c.getId();
                                            gifts.add(coin);
                                        }

                                        for (DocumentSnapshot document : documentsList) {
                                            DocumentReference docRef = db.collection("users").document(document.getId());
                                            double finalGiftsSum = giftsSum;
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> taskGifts) {
                                                    if (taskGifts.getResult().contains("piggybank")) {
                                                        // Remove the square brackets from the String of previous gifts stored in the database and split it into coins
                                                        String[] prevGifts = taskGifts.getResult().get("piggybank").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                                                        //gifts.addAll(Arrays.asList(prevGifts));
                                                        for (String c: prevGifts) {
                                                            if (!c.equals("")){
                                                                gifts.add(c);
                                                            }
                                                        }

                                                        DocumentReference docRefUsername = db.collection("users").document(user.getUid());
                                                        docRefUsername.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> taskGetUsername) {
                                                                if (taskGetUsername.isSuccessful()) {
                                                                    DocumentSnapshot documentCurrUser = taskGetUsername.getResult();
                                                                    if (documentCurrUser.exists()) {
                                                                        String username = documentCurrUser.getString("username");
                                                                        String notification = String.format(username + " sent you %.2f GOLD! You can view the gift in your Piggybank.", finalGiftsSum);
                                                                        notifications.add(notification);

                                                                        if (taskGifts.getResult().contains("notifications")) {
                                                                            String[] prevNotifications = taskGifts.getResult().get("notifications").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                                                                            for (String n: prevNotifications) {
                                                                                if (!n.equals("")) {
                                                                                    notifications.add(n);
                                                                                }
                                                                            }
                                                                        }
                                                                        db.collection("users").document(document.getId()).update("notifications", notifications);
                                                                        db.collection("users").document(document.getId()).update("newNotifications", true);

                                                                        users.add(username);
                                                                        if (taskGifts.getResult().contains("cantStealFrom")) {
                                                                            String[] prevUsers = taskGifts.getResult().get("cantStealFrom").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                                                                            for (String u: prevUsers) {
                                                                                if (!u.equals("")) {
                                                                                    users.add(u);
                                                                                }
                                                                            }
                                                                        }
                                                                        db.collection("users").document(document.getId()).update("cantStealFrom", users);
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
                                                    db.collection("users").document(document.getId()).update("piggybank", gifts);
                                                    Toast.makeText(SelectCoinGiftsActivity.this, "Gift sent to user.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        coinsSelected.clear();

                                        int coinsNum = MapActivity.walletCoins.size();
                                        double totalMoney = 0;
                                        for (Coin coin : MapActivity.walletCoins) {
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
                                        updateWallet();

                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(SelectCoinGiftsActivity.this, "You have to bank 25 coins first in order send coins to users. Coins left: " + task.getResult().get("coinsLeft").toString(), Toast.LENGTH_SHORT).show();
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
                    coinsSelected.addAll(MapActivity.walletCoins);

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
                }
                else {
                    adapter.selectedPositions.add(position);
                    v.setBackgroundResource(R.drawable.coin_selected);
                    Coin c = ImageAdapter.coins.get(position);
                    coinsSelected.add(c);
                }
            }
        });
    }

    private void updateWallet() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user  = firebaseAuth.getCurrentUser()  ;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> walletCoinsList = new ArrayList<>();

        for (Coin coin: MapActivity.walletCoins) {
            String coinStr = coin.getValue() + " " + coin.getCurrency() + " "  + coin.getId();
            walletCoinsList.add(coinStr);
        }

        db.collection("users").document(user.getUid()).update("wallet", walletCoinsList);

    }
}
