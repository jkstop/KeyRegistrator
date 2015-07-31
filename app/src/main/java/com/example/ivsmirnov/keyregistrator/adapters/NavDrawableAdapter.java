package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 22.07.2015.
 */
public class NavDrawableAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> items;
    private LayoutInflater inflater;

    public NavDrawableAdapter(Context c,ArrayList<String> it){
        this.context = c;
        this.items = it;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return items.indexOf(items.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = inflater.inflate(R.layout.layout_nav_drawable_item,parent,false);

        ImageView image = (ImageView)rootView.findViewById(R.id.image_nav);
        TextView text = (TextView)rootView.findViewById(R.id.text_nav);

        text.setText(items.get(position));
        if (items.get(position).equalsIgnoreCase("Настройки")){
            image.setImageResource(R.drawable.ic_settings_black_24dp);
        }else if (items.get(position).equalsIgnoreCase("Статистика")){
            image.setImageResource(R.drawable.ic_gradient_black_48dp);
        }else if (items.get(position).equalsIgnoreCase("Журнал")){
            image.setImageResource(R.drawable.ic_format_list_bulleted_black_48dp);
        }else{
            image.setImageResource(R.drawable.key);
        }

        return rootView;
    }
}
