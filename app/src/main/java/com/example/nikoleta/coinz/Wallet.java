package com.example.nikoleta.coinz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

public class Wallet extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        if (MapActivity.walletCoins.isEmpty()) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            List<String> walletCoinsList = new ArrayList<>();

            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().contains("wallet")) {
                        String[] prevCoins = task.getResult().get("wallet").toString().replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
                        if (!prevCoins[0].equals("")) {
                            for (String c : prevCoins) {
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

            DocumentReference docRef = db.collection("users").document(user.getUid());
            double finalTotalMoney = totalMoney;
            int coinsLeft;
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    int coinsLeft;
                    if (!task.getResult().contains("coinsLeft")) {
                        coinsLeft = 0;
                    } else {
                        coinsLeft = Integer.parseInt(task.getResult().get("coinsLeft").toString());
                    }
                    walletSummary.setText(String.format("Coins: %d\n\nCoins left: %d\n\nTotal:\n%.2f GOLD", coinsNum, coinsLeft, finalTotalMoney));
                }
            });


            Button btnBank = (Button) findViewById(R.id.send_to_bank);
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
                            int coinsLeft;
                            if (!task.getResult().contains("coinsLeft")) {
                                coinsLeft = 0;
                            }
                            else {
                                coinsLeft = Integer.parseInt(task.getResult().get("coinsLeft").toString());
                            }
                            if (coinsSelected.size() > coinsLeft) {
                                Toast.makeText(Wallet.this, String.format("You have a daily limit of 25 coins to send to Bank. Coins left for transfer: %d", coinsLeft), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                coinsLeft = coinsLeft - coinsSelected.size();
                                db.collection("users").document(user.getUid()).update("coinsLeft", coinsLeft);

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
                                Toast.makeText(Wallet.this, "Money in Bank: " + String.format("%.2f", moneyBank + money), Toast.LENGTH_SHORT).show();

                                MapActivity.walletCoins.removeAll(coinsSelected);
                                adapter.notifyDataSetChanged();

                                adapter.selectedPositions.clear();
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
                                walletSummary.setText(String.format("Coins: %d\n\nCoins left: %d\n\nTotal:\n%.2f GOLD", coinsNum, coinsLeft, totalMoney));

                                updateWallet();
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
                    } else {
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
