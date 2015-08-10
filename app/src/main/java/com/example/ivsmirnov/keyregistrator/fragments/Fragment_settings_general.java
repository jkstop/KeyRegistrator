package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.FileManager;

/**
 * Created by ivsmirnov on 18.06.2015.
 */
public class Fragment_settings_general extends android.support.v4.app.Fragment {

    private ListView listView;
    private Context context;
    private String path;
    private String [] items;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    Boolean click;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_general,container,false);
        context = rootView.getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        path = preferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL, "Не выбрано");
        items = context.getResources().getStringArray(R.array.db_share_items);
        listView = (ListView)rootView.findViewById(R.id.fragment_list_general);
        listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_2,android.R.id.text1,items){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView,parent);
                TextView text1 = (TextView)view.findViewById(android.R.id.text1);
                TextView text2 = (TextView)view.findViewById(android.R.id.text2);
                text1.setText(items[position]);
                if (position==0){
                    text2.setText(path);
                }
                text1.setGravity(Gravity.CENTER);
                text1.setTextColor(Color.BLACK);
                text2.setGravity(Gravity.CENTER);
                text2.setTextColor(Color.BLACK);
                return view;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent startFileManager = new Intent(context, FileManager.class);
                        startFileManager.putExtra("buttonChoise", true);
                        startActivity(startFileManager);
                        break;
                    case 1:
                        Dialog_Fragment dialog = new Dialog_Fragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(Values.DIALOG_TYPE, Values.DIALOG_SEEKBAR);
                        dialog.setArguments(bundle);
                        dialog.show(getFragmentManager(), "seek");
                        break;
                    case 2:
                        Dialog_Fragment dialogImage = new Dialog_Fragment();
                        Bundle bundle2 = new Bundle();
                        bundle2.putInt(Values.DIALOG_TYPE,67);
                        dialogImage.setArguments(bundle2);
                        dialogImage.show(getFragmentManager(),"image");
                        break;
                    default:
                        break;
                }

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        path = preferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL, "Не выбрано");
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        adapter.notifyDataSetChanged();
    }

}
