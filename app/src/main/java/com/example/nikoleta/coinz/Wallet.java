package com.example.nikoleta.coinz;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Wallet extends AppCompatActivity {

    static final String[] ITEM_LIST = new String[] { "QUID", "SHIL",
            "PENY", "DOLR" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Coin[] coins = {new Coin("QUID", 5.22, "sdfaf"), new Coin("DOLR", 6.86, "shsfhsj"), new Coin("PENY", 4.9, "sfsfsf"), new Coin("SHIL", 3.3333333333, "setqtqt"), new Coin("QUID", 5.22, "sdfaf"), new Coin("DOLR", 6.86, "shsfhsj"), new Coin("PENY", 4.9, "sfsfsf"), new Coin("SHIL", 3.3333333333, "setqtqt"), new Coin("QUID", 5.22, "sdfaf"), new Coin("DOLR", 6.86, "shsfhsj"), new Coin("PENY", 4.9, "sfsfsf"), new Coin("SHIL", 3.3333333333, "setqtqt"), new Coin("QUID", 5.22, "sdfaf"), new Coin("DOLR", 6.86, "shsfhsj"), new Coin("PENY", 4.9, "sfsfsf"), new Coin("SHIL", 3.3333333333, "setqtqt")};

        List<ImageAdapter.GridItem> coinsSelected = new ArrayList<>();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), coins);
        gridview.setAdapter(adapter);
        TextView walletSummary = (TextView) findViewById(R.id.coins);
        int coinsNum = coins.length;
        double totalMoney = 0;
        for (Coin coin : coins) {
            totalMoney = totalMoney + coin.getValue();
        }
        walletSummary.setText(String.format("Coins: \n%d\n\nTotal:\n%.2f", coinsNum, totalMoney));

        Button btnBank = (Button) findViewById(R.id.send_to_bank);
        btnBank.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(Wallet.this, "" + position,
//                        Toast.LENGTH_SHORT).show();

//                int selectedIndex = adapter.selectedPositions.indexOf(position);
//                if (selectedIndex > -1) {
//                    adapter.selectedPositions.remove(selectedIndex);
//                    ((GridItemView) v).display(false);
//                    //coinsSelected.remove((Coin) parent.getItemAtPosition(position));
//                } else {
//                    adapter.selectedPositions.add(position);
//                    ((GridItemView) v).display(true);
//                    //coinsSelected.add((Coin) parent.getItemAtPosition(position));
//                }
                //boolean isSelected = coinsSelected.get(position).isSelected();
                //coinsSelected.get(position).setSelected(!isSelected);
            }
        });
    }

    public static class GridItemView extends FrameLayout {

        private ImageView imageView;
        private TextView textView;

        public GridItemView(Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.grid_item, this);
            imageView = (ImageView) getRootView().findViewById(R.id.grid_image);
            textView = (TextView) getRootView().findViewById(R.id.grid_label);
        }

//        public void display(String text, boolean isSelected) {
//            //textView.setText(text);
//            display(isSelected);
//        }

        public void display(boolean isSelected) {
            //imageView.setBackgroundResource(isSelected ? R.drawable.coin_selected : R.drawable.coin_not_selected);
            //textView.setBackgroundResource(isSelected ? R.drawable.coin_selected : R.drawable.coin_not_selected);
        }
    }
}
