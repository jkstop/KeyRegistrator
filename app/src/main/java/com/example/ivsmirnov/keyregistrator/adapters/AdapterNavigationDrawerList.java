package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.items.NavigationItem;

import java.util.ArrayList;

/**
 * адаптер бокового меню
 */
public class AdapterNavigationDrawerList extends ArrayAdapter<NavigationItem> {

    private Context mContext;
    private ArrayList<NavigationItem> mItems;
    private LayoutInflater mInflater;

    public AdapterNavigationDrawerList(Context context, ArrayList<NavigationItem> arrays) {
        super(context, R.layout.navigation_drawer,arrays);
        this.mContext = context;
        this.mItems = arrays;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        if (mItems.get(position).getSeparator()){
            rootView = mInflater.inflate(R.layout.navigation_drawer_list_separator,parent,false);
        }else{
            rootView = mInflater.inflate(R.layout.navigation_drawer_list_item,parent,false);

            if (mItems.get(position).getSelected()){
                rootView.setBackgroundColor(Color.LTGRAY);
            } else {
                rootView.setBackgroundColor(Color.TRANSPARENT);
            }

            ImageView mImage = (ImageView) rootView.findViewById(R.id.navigation_drawer_list_item_image);
            TextView mText = (TextView) rootView.findViewById(R.id.navigation_drawer_list_item_text);
            mText.setText(mItems.get(position).getText());
            mImage.setImageResource(mItems.get(position).getDraw());

        }
        return rootView;
    }


}
