package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 17.06.2015.
 */
public class ListRoomsAdapter extends ArrayAdapter <Integer> {

    private ArrayList<Integer> items;
    private Context context;

    public ListRoomsAdapter(Context context, ArrayList<Integer> items) {
        super(context, R.layout.list_room_item,items);
        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_room_item,parent,false);

        TextView text = (TextView)rowView.findViewById(R.id.text_rooms);
        text.setText(String.valueOf(items.get(position)));


        return rowView;
    }
}
