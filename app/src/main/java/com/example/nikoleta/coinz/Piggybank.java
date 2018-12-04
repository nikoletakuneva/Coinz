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
import java.util.List;

public class Piggybank extends AppCompatActivity {
    List<String> piggybankCoinsList = new ArrayList<>();
    List<Coin> piggybankCoins = new ArrayList<>();

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
                if (task.getResult().contains("gifts")) {
                    String[] prevCoins = task.getResult().get("gifts").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                    if (!prevCoins[0].equals("")) {
                        for (String c : prevCoins) {
                            piggybankCoinsList.add(c);
                            String[] coinProperties = c.split(" ");
                            Coin coin = new Coin(coinProperties[1], Double.parseDouble(coinProperties[0]), coinProperties[2]);
                            piggybankCoins.add(coin);
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
                                moneyBank = moneyBank + coin.getValue() * rate;
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
                        piggybankSummary.setText(String.format("Coins: %d\n\nTotal:\n%.2f GOLD", coinsNum, totalMoney));

                            updatePiggybank();

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
                if (selectedIndex > -1) {
                    adapter.selectedPositions.remove(selectedIndex);
                    v.setBackgroundResource(R.drawable.coin_not_selected);
                    Coin c = ImageAdapter.coins.get(position);
                    coinsSelected.remove(c);
                } else {
                    adapter.selectedPositions.add(position);
                    v.setBackgroundResource(R.drawable.coin_selected);
                    Coin c = ImageAdapter.coins.get(position);
                    coinsSelected.add(c);
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

        db.collection("users").document(user.getUid()).update("gifts", piggybankCoinsList);
    }
}
