package com.example.nikoleta.coinz;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Piggybank extends AppCompatActivity {
    List<String> piggybankCoinsList = new ArrayList<>();
    List<Coin> piggybankCoins = new ArrayList<>();
    HashMap<String, Coin> magnetCollection = new HashMap<>();
    HashMap<String, Coin> stealCollection = new HashMap<>();
    HashMap<String, Coin> shieldCollection = new HashMap<>();
    final String[] MODES = {"NONE", "MAGNET", "STEAL", "SHIELD"};
    final double MAGNET_COST = 50.00;
    final double STEAL_COST = 500.00;
    final double SHIELD_COST = 800.00;
    String MODE = "NONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piggybank);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().contains("piggybank")) {
                    String[] prevCoins = task.getResult().get("piggybank").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
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
            }
        });
    }

    private void piggybankView() {
        List<Coin> coinsSelected = new ArrayList<>();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), piggybankCoins);
        gridview.setAdapter(adapter);
        TextView piggybankSummary = (TextView) findViewById(R.id.coins);
        int coinsNum = piggybankCoins.size();
        double totalMoney = 0;
        for (Coin coin : piggybankCoins) {
            totalMoney = totalMoney + coin.getGOLDValue();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        DocumentReference docRef = db.collection("users").document(user.getUid());
        double finalTotalMoney = totalMoney;

        piggybankSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum, finalTotalMoney));

        Button btnBank = (Button) findViewById(R.id.send_bank);
        btnBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                DocumentReference docRef = db.collection("users").document(user.getUid());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            double moneyBank = 0.0;
                            for (Coin coin : coinsSelected) {
                                moneyBank = moneyBank + coin.getGOLDValue();
                            }
                            double money;
                            if (!task.getResult().contains("money")) {
                                money = 0.0;
                            } else {
                                money = Double.parseDouble(task.getResult().get("money").toString());
                            }
                            db.collection("users").document(user.getUid()).update("money", moneyBank + money);
                            Toast.makeText(Piggybank.this, "Money in Bank: " + String.format("%.2f", moneyBank + money), Toast.LENGTH_SHORT).show();

                            piggybankCoins.removeAll(coinsSelected);
                            adapter.notifyDataSetChanged();

                            adapter.selectedPositions.clear();
                            coinsSelected.clear();


                            int coinsNum = piggybankCoins.size();
                            double totalMoney = 0;
                            for (Coin coin : piggybankCoins) {
                                totalMoney = totalMoney + coin.getGOLDValue();
                            }
                            piggybankSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum, totalMoney));
                            updatePiggybank();
                    }
                });
            }
        });

        Button btnMagnet = (Button) findViewById(R.id.unlock_magnet);
        btnMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MODE = "MAGNET";
                Toast.makeText(getApplicationContext(),
                        "Please select 4 coins of the different currencies, each with value more than "
                                + MAGNET_COST + " GOLD", Toast.LENGTH_LONG).show();
            }
        });

        Button btnSteal = (Button) findViewById(R.id.unlock_thief);
        btnSteal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MODE = "STEAL";
                Toast.makeText(getApplicationContext(),
                        "Please select 4 coins of the different currencies, each with value more than "
                                + STEAL_COST + " GOLD", Toast.LENGTH_LONG).show();
            }
        });

        Button btnShield = (Button) findViewById(R.id.unlock_shield);
        btnShield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MODE = "SHIELD";
                Toast.makeText(getApplicationContext(),
                        "Please select 4 coins of the different currencies, each with value more than "
                                + SHIELD_COST + " GOLD", Toast.LENGTH_LONG).show();
            }
        });


        Button selectAll = (Button) findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();

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

        db.collection("users").document(user.getUid()).update("piggybank", piggybankCoinsList);
    }
}
