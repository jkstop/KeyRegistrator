package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.NavigationItem;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 17.12.2015.
 */
public class adapter_navigation_drawer_list extends ArrayAdapter<NavigationItem> {

    private Context mContext;
    private ArrayList<NavigationItem> mItems;
    private LayoutInflater mInflater;
    private SparseArray mCard;
    private ImageView mImage;
    private TextView mText;


    public adapter_navigation_drawer_list(Context context, ArrayList<NavigationItem> arrays) {
        super(context, R.layout.navigation_drawer,arrays);
        this.mContext = context;
        this.mItems = arrays;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        if (mItems.get(position).mSeparator){
            if (convertView==null){
                rootView = mInflater.inflate(R.layout.navigation_drawer_list_separator,parent,false);
            }else{
                rootView = convertView;
            }
        }else{
            if (convertView==null){
                rootView = mInflater.inflate(R.layout.navigation_drawer_list_item,parent,false);
            }else{
                rootView = convertView;
            }
            mImage = (ImageView)rootView.findViewById(R.id.navigation_drawer_list_item_image);
            mText = (TextView)rootView.findViewById(R.id.navigation_drawer_list_item_text);
            mText.setText(mItems.get(position).mText);
            mImage.setImageResource(mItems.get(position).mDraw);
        }
        return rootView;
    }

    public String getItemText(int position){
        return mItems.get(position).mText;
    }
}
