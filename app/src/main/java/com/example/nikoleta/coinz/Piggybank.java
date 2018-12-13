package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Piggybank extends AppCompatActivity {
    List<String> piggybankCoinsList = new ArrayList<>();
    List<Coin> piggybankCoins = new ArrayList<>();
    HashMap<String, Coin> magnetCollection = new HashMap<>();
    HashMap<String, Coin> stealCollection = new HashMap<>();
    HashMap<String, Coin> shieldCollection = new HashMap<>();
    //final String[] MODES = {"NONE", "MAGNET", "STEAL", "SHIELD"};
    final double MAGNET_COST = 100.00;
    final double STEAL_COST = 300.00;
    final double SHIELD_COST = 400.00;
    String MODE = "NONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piggybank);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(Objects.requireNonNull(user).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (Objects.requireNonNull(task.getResult()).contains("piggybank")) {
                String[] prevCoins = Objects.requireNonNull(task.getResult().get("piggybank")).toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
                if (!prevCoins[0].equals("")) {
                    for (String c : prevCoins) {
                        if(!c.equals("")){
                            piggybankCoinsList.add(c);
                            String[] coinProperties = c.split(" ");
                            Coin coin = new Coin(coinProperties[1], Double.parseDouble(coinProperties[0]), coinProperties[2]);
                            piggybankCoins.add(coin);
                        }

                    }
                }
            }
            piggybankView();
        });
    }

    @SuppressLint("DefaultLocale")
    private void piggybankView() {
        List<Coin> coinsSelected = new ArrayList<>();

        GridView gridview = findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), piggybankCoins);
        gridview.setAdapter(adapter);
        TextView piggybankSummary = findViewById(R.id.coins);
        int coinsNum = piggybankCoins.size();
        double totalMoney = 0;
        for (Coin coin : piggybankCoins) {
            totalMoney = totalMoney + coin.getGOLDValue();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        assert user != null;
        double finalTotalMoney = totalMoney;

        piggybankSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum, finalTotalMoney));

        Button btnBank = findViewById(R.id.send_bank);
        btnBank.setOnClickListener(v -> {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                double moneyBank = 0.0;
                for (Coin coin : coinsSelected) {
                    moneyBank = moneyBank + coin.getGOLDValue();
                }
                double money;
                if (!Objects.requireNonNull(task.getResult()).contains("money")) {
                    money = 0.0;
                }
                else {
                    money = Double.parseDouble(Objects.requireNonNull(task.getResult().get("money")).toString());
                }
                db.collection("users").document(user.getUid()).update("money", moneyBank + money);
                Toast.makeText(Piggybank.this, "Money in Bank: " + String.format("%.2f", moneyBank + money), Toast.LENGTH_SHORT).show();

                piggybankCoins.removeAll(coinsSelected);
                adapter.notifyDataSetChanged();

                adapter.selectedPositions.clear();
                coinsSelected.clear();

                int coinsNum1 = piggybankCoins.size();
                double totalMoney1 = 0;
                for (Coin coin : piggybankCoins) {
                    totalMoney1 = totalMoney1 + coin.getGOLDValue();
                }
                piggybankSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum1, totalMoney1));
                updatePiggybank();
            });
        });

        Button btnMagnet = findViewById(R.id.unlock_magnet);
        btnMagnet.setOnClickListener(view -> {
            MODE = "MAGNET";
            Toast.makeText(getApplicationContext(),
                    "Please select 4 coins of the different currencies, each with value more than "
                            + MAGNET_COST + " GOLD", Toast.LENGTH_LONG).show();
        });

        Button btnSteal = findViewById(R.id.unlock_thief);
        btnSteal.setOnClickListener(view -> {
            MODE = "STEAL";
            Toast.makeText(getApplicationContext(),
                    "Please select 4 coins of the different currencies, each with value more than "
                            + STEAL_COST + " GOLD", Toast.LENGTH_LONG).show();
        });

        Button btnShield = findViewById(R.id.unlock_shield);
        btnShield.setOnClickListener(view -> {
            MODE = "SHIELD";
            Toast.makeText(getApplicationContext(),
                    "Please select 4 coins of the different currencies, each with value more than "
                            + SHIELD_COST + " GOLD", Toast.LENGTH_LONG).show();
        });


        Button selectAll = findViewById(R.id.select_all);
        selectAll.setOnClickListener(v -> {
            if (coinsSelected.size() != coinsNum) {
                adapter.selectedPositions.clear();
                coinsSelected.clear();
                coinsSelected.addAll(piggybankCoins);

                for (int i = 0; i < coinsNum; i++) {
                    adapter.selectedPositions.add(i);
                    View viewItem = gridview.getChildAt(i);
                    if (viewItem != null) {
                        viewItem.setBackgroundResource(R.drawable.coin_selected);
                    }
                }
            }
        });

        Button deselectAll = findViewById(R.id.deselect_all);
        deselectAll.setOnClickListener(v -> {
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

        });

        gridview.setOnItemClickListener((parent, v, position, id) -> {
            int selectedIndex = adapter.selectedPositions.indexOf(position);
            Coin c = ImageAdapter.coins.get(position);
            if (selectedIndex > -1) {
                adapter.selectedPositions.remove(selectedIndex);
                v.setBackgroundResource(R.drawable.coin_not_selected);
                coinsSelected.remove(c);
            } else {
                adapter.selectedPositions.add(position);
                v.setBackgroundResource(R.drawable.coin_selected);
                coinsSelected.add(c);
            }

            switch (MODE) {
                case "NONE":
                    break;
                case "MAGNET":
                    if(c.getGOLDValue() >= MAGNET_COST) {
                        magnetCollection.put(c.getCurrency(), c);
                    }
                    if (magnetCollection.size() == 4) {
                        piggybankCoins.removeAll(coinsSelected);
                        adapter.notifyDataSetChanged();
                        adapter.selectedPositions.clear();
                        coinsSelected.clear();
                        db.collection("users").document(user.getUid()).update("magnetUnlocked", true);
                        Toast.makeText(getApplicationContext(),
                                "You have successfully unlocked the Magnet Booster!",
                                                      Toast.LENGTH_LONG).show();
                        updatePiggybank();
                        MODE = "NONE";
                    }
                    break;
                case "STEAL":
                    if(c.getGOLDValue() >= STEAL_COST) {
                        stealCollection.put(c.getCurrency(), c);
                    }
                    if (stealCollection.size() == 4) {
                        piggybankCoins.removeAll(coinsSelected);
                        adapter.notifyDataSetChanged();
                        adapter.selectedPositions.clear();
                        coinsSelected.clear();
                        db.collection("users").document(user.getUid()).update("stealUnlocked", true);
                        Toast.makeText(getApplicationContext(),
                                "You have successfully unlocked the Stealling Booster!",
                                Toast.LENGTH_LONG).show();
                        updatePiggybank();
                        MODE = "NONE";
                    }
                    break;
                case "SHIELD":
                    if(c.getGOLDValue() >= SHIELD_COST) {
                        shieldCollection.put(c.getCurrency(), c);
                    }
                    if (shieldCollection.size() == 4) {
                        piggybankCoins.removeAll(coinsSelected);
                        adapter.notifyDataSetChanged();
                        adapter.selectedPositions.clear();
                        coinsSelected.clear();
                        db.collection("users").document(user.getUid()).update("shieldUnlocked", true);
                        Toast.makeText(getApplicationContext(),
                                "You have successfully unlocked the Shield Booster!",
                                Toast.LENGTH_LONG).show();
                        updatePiggybank();
                        MODE = "NONE";
                    }
                    break;
            }
        });
    }

    private void updatePiggybank() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> piggybankCoinsList = new ArrayList<>();

        for (Coin coin : piggybankCoins) {
            String coinStr = coin.getValue() + " " + coin.getCurrency() + " " + coin.getId();
            piggybankCoinsList.add(coinStr);
        }

        assert user != null;
        db.collection("users").document(user.getUid()).update("piggybank", piggybankCoinsList);
    }
}
