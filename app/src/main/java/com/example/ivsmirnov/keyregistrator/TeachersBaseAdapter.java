package com.example.ivsmirnov.keyregistrator;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TeachersBaseAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList <String> arrayOfTeachers;


    public TeachersBaseAdapter(Context c, ArrayList<String> array){
        context = c;
        arrayOfTeachers = array;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrayOfTeachers.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return arrayOfTeachers.indexOf(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView==null){
            view = inflater.inflate(R.layout.cell_for_teachers_base,null);
        }else{
            view = convertView;
        }
        ImageView image = (ImageView)view.findViewById(R.id.imagePerson);
        TextView text = (TextView)view.findViewById(R.id.textPerson);

        //button = (Button)view.findViewById(R.id.grid_item_teachers_base);
        //button.setBackgroundResource(R.drawable.button_grid_background);
        //view.setPadding(5, 15, 5, 15);

        if (arrayOfTeachers.get(position).equals("яяя")){
            image.setImageResource(R.drawable.add);
            text.setText("Добавить");
            view.setTag("add");
            view.setBackgroundResource(R.drawable.button_background);
        }else{
            image.setImageResource(R.drawable.person);
            text.setText(arrayOfTeachers.get(position));
            view.setTag(position);
            view.setBackgroundResource(R.drawable.button_grid_background);
        }

        return view;
    }
}
