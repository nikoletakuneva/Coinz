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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Wallet extends AppCompatActivity {
    static final String[] ITEM_LIST = new String[] { "QUID", "SHIL",
            "PENY", "DOLR" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Coin[] coinsArr = {new Coin("QUID", 5.22, "sdfaf"), new Coin("DOLR", 6.86, "shsfhsj"), new Coin("PENY", 4.9, "sfsfsf"), new Coin("SHIL", 3.3333333333, "setqtqt"), new Coin("QUID", 5.22, "sdfhchf"), new Coin("DOLR", 6.86, "sfchfchhsfhsj"), new Coin("PENY", 4.9, "sfghkgjsfsf"), new Coin("SHIL", 3.3333333333, "setqfjuftuitqt"), new Coin("QUID", 5.22, "sddydyfaf"), new Coin("DOLR", 6.86, "shsypypfhsj"), new Coin("PENY", 4.9, "sfsfwwwsf"), new Coin("SHIL", 3.3333333333, "setsssqtqt"), new Coin("QUID", 5.22, "dry"), new Coin("DOLR", 6.86, "shsftutuirhsj"), new Coin("PENY", 4.9, "sfsfsuuuf"), new Coin("SHIL", 3.3333333333, "sedrysywytqtqt")};
        List<Coin> coins = Arrays.asList(coinsArr);
        List<Coin> coinsSelected = new ArrayList<>();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), coins);
        gridview.setAdapter(adapter);
        TextView walletSummary = (TextView) findViewById(R.id.coins);
        int coinsNum = coins.size();
        double totalMoney = 0;
        for (Coin coin : coins) {
            totalMoney = totalMoney + coin.getValue();
        }
        walletSummary.setText(String.format("Coins: \n%d\n\nTotal:\n%.2f GOLD", coinsNum, totalMoney));

        Button btnBank = (Button) findViewById(R.id.send_to_bank);
        btnBank.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                String id = user.getUid();
                DocumentReference docRef = db.collection("users").document(user.getUid());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        double moneyBank=0.0;
                        for (Coin coin : coinsSelected) {
                            moneyBank = moneyBank + coin.getValue();
                        }
                        double money;
                        if (!task.getResult().contains("money")) {
                            money = 0.0;
                        }
                        else {
                            money = Double.parseDouble(task.getResult().get("money").toString());
                        }
                        db.collection("users").document(user.getUid()).update("money", moneyBank + money);
                        Toast.makeText(Wallet.this, "Money in Bank: " + String.format("%.2f", moneyBank + money),  Toast.LENGTH_SHORT).show();
                    }
                });



            }
        });

        Button selectAll = (Button) findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (coinsSelected.size() != coinsNum) {
                    adapter.selectedPositions.clear();
                    coinsSelected.clear();
                    coinsSelected.addAll(coins);
                    for (int i=0; i<coinsNum; i++) {
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
        deselectAll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (coinsSelected.size() != 0) {
                    adapter.selectedPositions.clear();
                    coinsSelected.clear();
                    for (int i=0; i<coinsNum; i++) {
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
}
