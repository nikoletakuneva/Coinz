package com.example.nikoleta.coinz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] itemName;
    // references to our images
    private Integer[] mThumbIds;

    public ImageAdapter(Context c, String[] itemName, Integer[] thumbIDs) {
        mContext = c;
        this.itemName = itemName;
        this.mThumbIds = thumbIDs;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView==null) {

            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null);
            holder.textView = (TextView) convertView.findViewById(R.id.grid_label);
            holder.imageView = (ImageView) convertView.findViewById(R.id.grid_image);

            convertView.setTag(holder);
        }else{

            holder = (ViewHolder)convertView.getTag();
        }

        holder.textView.setText(itemName[position]);

        if (itemName[position].equalsIgnoreCase("DOLR")){
            holder.imageView.setImageResource(R.drawable.dollar);
        }else if (itemName[position].equalsIgnoreCase("SHIL")){
            holder.imageView.setImageResource(R.drawable.shilling);
        }else if (itemName[position].equalsIgnoreCase("QUID")){
            holder.imageView.setImageResource(R.drawable.quid);
        }else {
            holder.imageView.setImageResource(R.drawable.penny);
        }

        return convertView;
    }

    class ViewHolder{
        TextView textView;
        ImageView imageView;
    }
}
