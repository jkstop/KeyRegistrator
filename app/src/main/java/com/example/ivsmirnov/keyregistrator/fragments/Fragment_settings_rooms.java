package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.Rooms;

/**
 * Created by ivsmirnov on 18.06.2015.
 */
public class Fragment_settings_rooms extends Fragment {

    private ListView listView;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_rooms,container,false);
        context = rootView.getContext();
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        listView = (ListView)rootView.findViewById(R.id.fragment_list_rooms);
        listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,context.getResources().getStringArray(R.array.db_rooms_items)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View root = super.getView(position, convertView, parent);
                TextView text = (TextView)root.findViewById(android.R.id.text1);
                text.setGravity(Gravity.CENTER);
                text.setTextColor(Color.BLACK);
                return root;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(context, Rooms.class));
                        break;
                    case 1:
                        showColumnsCount(view);
                        break;
                    default:
                        break;
                }
            }
        });
        return rootView;
    }

    private void showColumnsCount(View v){
        PopupMenu popupMenu = new PopupMenu(context,v);
        popupMenu.inflate(R.menu.popup_columns);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.two:
                        editor.putInt(Values.COLUMNS_COUNT, 2);
                        editor.commit();
                        return true;
                    case R.id.three:
                        editor.putInt(Values.COLUMNS_COUNT, 3);
                        editor.commit();
                        return true;
                    case R.id.four:
                        editor.putInt(Values.COLUMNS_COUNT, 4);
                        editor.commit();
                        return true;
                    case R.id.five:
                        editor.putInt(Values.COLUMNS_COUNT, 5);
                        editor.commit();
                        return true;
                    default:
                        return false;
                }
            }
        });

        for (int i = 0;i<popupMenu.getMenu().size();i++){
            MenuItem menuItem = popupMenu.getMenu().getItem(i);
            if (menuItem.getTitle().equals(String.valueOf(preferences.getInt(Values.COLUMNS_COUNT, 1)))){
                menuItem.setChecked(true);
            }
        }

        popupMenu.show();
    }
}
