package com.example.mizuho.natureremowidget;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mizuho.natureremowidget.R;

public class SpinnerIconAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int itemLayoutID;
    private int listLayoutID;
    private String[] names;
    private int[] imageIDs;

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    SpinnerIconAdapter(Context context,
                       int itemLayoutId,
                       int listLayoutId,
                       String[] spinnerItems,
                       String[] spinnerImages ){

        inflater = LayoutInflater.from(context);
        itemLayoutID = itemLayoutId;
        listLayoutID = listLayoutId;
        names = spinnerItems;
        imageIDs = new int[spinnerImages.length];
        Resources res = context.getResources();

        // 最初に画像IDを配列で取っておく
        for( int i = 0; i< spinnerImages.length; i++){
            imageIDs[i] = res.getIdentifier(spinnerImages[i],
                    "drawable", context.getPackageName());
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(itemLayoutID, null);
            holder = new ViewHolder();

            holder.imageView = convertView.findViewById(R.id.image_view);
            //holder.textView = convertView.findViewById(R.id.text_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(imageIDs[position]);
        //777holder.textView.setText(names[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position,
                                View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(listLayoutID, null);
            holder = new ViewHolder();

            holder.imageView = convertView.findViewById(R.id.image_view);
            holder.textView = convertView.findViewById(R.id.text_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(imageIDs[position]);
        holder.textView.setText(names[position]);

        return convertView;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}