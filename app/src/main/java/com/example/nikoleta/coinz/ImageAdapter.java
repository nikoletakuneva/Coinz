package com.example.nikoleta.coinz;

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
    //private final String[] itemName;
    // references to our images
    //private Integer[] mThumbIds;
    private List<Coin> coins;
    public List<Integer> selectedPositions;

    public ImageAdapter(Context c, List<Coin> coins) {
        this.mContext = c;
        this.coins = coins;
        selectedPositions = new ArrayList<Integer>();
    }

    public int getCount() {
        return coins.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItem holder;
        Wallet.GridItemView customView;
        if (convertView==null) {
            holder = new GridItem();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null);
            holder.textView = (TextView) convertView.findViewById(R.id.grid_label);
            holder.imageView = (ImageView) convertView.findViewById(R.id.grid_image);
            convertView.setTag(holder);
            //customView = new Wallet.GridItemView(mContext);
        }
        else {
            holder = (GridItem)convertView.getTag();
            //customView = (Wallet.GridItemView) convertView;

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

        //customView.display(coins[position].getCurrency(), selectedPositions.contains(position));
        //customView.display(selectedPositions.contains(position));
        boolean isSelected = selectedPositions.contains(position);

        if (isSelected){
            convertView.setBackgroundResource(R.drawable.coin_selected);
        } else {
            convertView.setBackgroundResource(R.drawable.coin_not_selected);
        }
        //holder.imageView.setOnClickListener(v -> holder.imageView.setBackgroundResource(isSelected ? R.drawable.coin_selected : R.drawable.coin_not_selected));
        return convertView;
    }

    class GridItem{
        private TextView textView;
        private ImageView imageView;
        boolean selected=false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean s) {
            this.selected = s;
        }
    }
}
