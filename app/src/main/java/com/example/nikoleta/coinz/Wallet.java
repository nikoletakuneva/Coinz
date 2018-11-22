package com.example.nikoleta.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class Wallet extends AppCompatActivity {

    static final String[] ITEM_LIST = new String[] { "QUID", "SHIL",
            "PENNY", "DOLR" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this, ITEM_LIST,new Integer[]{
                R.drawable.dollar, R.drawable.quid,
                R.drawable.shilling, R.drawable.penny,
        }));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(Wallet.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
