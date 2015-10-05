package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 03.10.2015.
 */
public class adapter_list_characters extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> mCharacters;

    public adapter_list_characters(Context context, ArrayList<String> characters) {
        super(context, R.layout.row_for_characters_list,characters);
        this.mContext = context;
        this.mCharacters = characters;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.row_for_characters_list,parent,false);
        TextView text = (TextView)rootView.findViewById(R.id.text_for_row_character_list);
        text.setText(mCharacters.get(position));
        return rootView;
    }
}