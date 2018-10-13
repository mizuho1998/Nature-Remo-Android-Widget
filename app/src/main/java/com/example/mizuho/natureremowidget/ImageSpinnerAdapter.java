package com.example.mizuho.natureremowidget;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


public class ImageSpinnerAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int layoutID;
    private int[] imageIDs;

    static class ViewHolder {
        ImageView imageView;
    }

    ImageSpinnerAdapter(Context context,
                        int itemLayoutId,
                        String[] spinnerImages ){

        inflater = LayoutInflater.from(context);
        layoutID = itemLayoutId;
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
            convertView = inflater.inflate(layoutID, null);
            holder = new ViewHolder();

            holder.imageView = convertView.findViewById(R.id.image_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(imageIDs[position]);

        return convertView;
    }

    /*
    // 最初の要素を選択不可にする
    public boolean isEnabled(int position) {
        if (position == 0)
            return false;
        else
            return true;
    }
    */

    @Override
    public int getCount() {
        return imageIDs.length;
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
