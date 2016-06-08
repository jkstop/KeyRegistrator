package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.fragments.PersonsFr;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.others.App;

import java.util.ArrayList;

/**
 * адпатер для списка букв поиска
 */
public class AdapterPersonsCharacters extends ArrayAdapter<CharacterItem> {

    private Context mContext;
    private ArrayList<CharacterItem> mCharacters;

    public AdapterPersonsCharacters(Context context, ArrayList<CharacterItem> characters) {
        super(context, R.layout.row_for_characters_list,characters);
        this.mContext = context;
        this.mCharacters = characters;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.row_for_characters_list, parent, false);
        TextView text = (TextView)rootView.findViewById(R.id.text_for_row_character_list);
        text.setText(mCharacters.get(position).getCharacter());
        if (mCharacters.get(position).getSelection()){
            rootView.setBackgroundResource(R.drawable.character_background);
        }

        return rootView;
    }
}
