package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;


public class adapter_edit_auditrooms_grid extends BaseAdapter {

    private ArrayList<Integer> items;
    private Context context;
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;

    public adapter_edit_auditrooms_grid(Context c, ArrayList<Integer> i) {
        this.context = c;
        this.items = i;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.indexOf(items.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        if (convertView==null){
            rootView = inflater.inflate(R.layout.cell_for_grid_is_free,parent,false);
        }else{
            rootView = convertView;
        }

        TextView text = (TextView)rootView.findViewById(R.id.textButton);
        text.setText(items.get(position).toString());

        ImageView imageView = (ImageView)rootView.findViewById(R.id.imageButton);
        imageView.setImageResource(R.drawable.key_colored);

        rootView.setBackgroundResource(R.drawable.button_background);

        int space = (int) context.getResources().getDimension(R.dimen.grid_vertical_spacing);
        //int heightGrid = Main_Fragment.gridView.getHeight();
        //int childCount = Main_Fragment.gridView.getCount();
        int rows;
        int btnHeight = /*heightGrid/rows - space*/180;

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                btnHeight);
        rootView.setLayoutParams(layoutParams);
        return rootView;
    }
}
