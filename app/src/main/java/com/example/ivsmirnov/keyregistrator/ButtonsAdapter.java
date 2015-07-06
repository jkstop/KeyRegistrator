package com.example.ivsmirnov.keyregistrator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ButtonsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Integer> items;
    private ArrayList<Boolean> isFree;
    private ArrayList<String> lastVisiters;
    private LayoutInflater inflater;
    private SharedPreferences preferences;


    public ButtonsAdapter(Context c, ArrayList<Integer> i,ArrayList<Boolean> isF,ArrayList<String> sT){
        context = c;
        items = i;
        isFree = isF;
        lastVisiters = sT;
        inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

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
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;

            Button button;
            if (convertView == null) {
                view = inflater.inflate(R.layout.cell_for_grid, null);
            }else{
                view = convertView;
            }
            ImageView image = (ImageView)view.findViewById(R.id.imageButton);
            TextView text = (TextView)view.findViewById(R.id.textButton);

            image.setImageResource(R.drawable.key_colored);
            int space = (int) context.getResources().getDimension(R.dimen.grid_vertical_spacing);
            int heightGrid = Launcher.gridView.getHeight();
            int childCount = Launcher.gridView.getCount();
            int rows;
            rows = (int)Math.ceil((double)childCount/preferences.getInt(Values.COLUMNS_COUNT,1));
            int btnHeight = heightGrid/rows - space;


            text.setText(String.valueOf(items.get(position)));
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    btnHeight);
            view.setLayoutParams(layoutParams);
            if (isFree.get(position)){
                view.setBackgroundResource(R.drawable.button_background);

            }else{
                if (lastVisiters.get(position).equalsIgnoreCase("Центр поддержки")){
                    view.setBackgroundResource(R.drawable.button_background_support);
                }else{
                    view.setBackgroundResource(R.drawable.button_background_selected);
                }
                text.setText(String.valueOf(items.get(position))+ "\n"+lastVisiters.get(position));
                //textAud.setText(String.valueOf(items[position]));
                //text.setTextSize(heightGrid*0.35f*0.35f);
            }
            //view.setPadding(5,0,0,0);
            view.setTag(items.get(position));

        return view;
    }
}
