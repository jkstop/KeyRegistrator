package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 03.10.2015.
 */
public class adapter_list_characters extends ArrayAdapter<CharacterItem> {

    private Context mContext;
    private ArrayList<CharacterItem> mCharacters;

    public adapter_list_characters(Context context, ArrayList<CharacterItem> characters) {
        super(context, R.layout.row_for_characters_list,characters);
        this.mContext = context;
        this.mCharacters = characters;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.row_for_characters_list,parent,false);
        TextView text = (TextView)rootView.findViewById(R.id.text_for_row_character_list);
        text.setText(mCharacters.get(position).getCharacter());
        if (mCharacters.get(position).getSelection()){
            rootView.setBackgroundResource(R.drawable.character_background);
        }

        return rootView;
    }
}
