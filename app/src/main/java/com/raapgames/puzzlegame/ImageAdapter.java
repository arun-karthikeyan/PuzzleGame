package com.raapgames.puzzlegame;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Ponneeswaran on 12/5/2016.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
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
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            int width = (mContext.getResources().getDisplayMetrics().widthPixels-16)/2;
            imageView.setLayoutParams(new GridView.LayoutParams(width, width));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    public Integer[] getmThumbIds() {
        return mThumbIds;
    }

    public void setmThumbIds(Integer[] mThumbIds) {
        this.mThumbIds = mThumbIds;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.finding_nemo,
            R.drawable.batman,
            R.drawable.pikachu,
            R.drawable.charizard,
            R.drawable.racoon,
            R.drawable.wolf,
            R.drawable.fall_art,
            R.drawable.sports_art,
            R.drawable.nature_path,
            R.drawable.nature_grass
    };
}
