package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ImageAdapter extends BaseAdapter {
    private Context mContext;
    public static List<Coin> coins;
    public List<Integer> selectedPositions;

    ImageAdapter(Context c, List<Coin> coins) {
        this.mContext = c;
        ImageAdapter.coins = coins;
        selectedPositions = new ArrayList<>();
    }

    public int getCount() {
        return coins.size();
    }

    public Coin getItem(int position) {
        return coins.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @SuppressLint({"InflateParams", "SetTextI18n", "DefaultLocale"})
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItem holder;
        if (convertView==null) {
            holder = new GridItem(coins.get(position));
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null);
            holder.textView = convertView.findViewById(R.id.grid_label);
            holder.imageView = convertView.findViewById(R.id.grid_image);
            convertView.setTag(holder);
        }
        else {
            holder = (GridItem)convertView.getTag();

        }
        String currency = coins.get(position).getCurrency();
        double value = coins.get(position).getValue();
        holder.textView.setText(String.format("%.2f", value) + " " + currency);

        if (currency.equalsIgnoreCase("DOLR")){
            holder.imageView.setImageResource(R.drawable.dollar);
        }else if (currency.equalsIgnoreCase("SHIL")){
            holder.imageView.setImageResource(R.drawable.shilling);
        }else if (currency.equalsIgnoreCase("QUID")){
            holder.imageView.setImageResource(R.drawable.quid);
        }else {
            holder.imageView.setImageResource(R.drawable.penny);
        }

        boolean isSelected = selectedPositions.contains(position);

        if (isSelected){
            convertView.setBackgroundResource(R.drawable.coin_selected);
        } else {
            convertView.setBackgroundResource(R.drawable.coin_not_selected);
        }
        return convertView;
    }

    class GridItem{
        private TextView textView;
        private ImageView imageView;
        private Coin coin;

        GridItem(Coin coin) {
            this.coin = coin;
        }

        public Coin getCoin() {
            return coin;
        }

        public void setCoin(Coin coin) {
            this.coin = coin;
        }
    }
}
