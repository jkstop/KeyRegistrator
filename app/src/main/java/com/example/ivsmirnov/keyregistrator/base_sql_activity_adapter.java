package com.example.ivsmirnov.keyregistrator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity_adapter extends BaseAdapter {

    private SparseArray <String> card;

    private ArrayList <SparseArray> allItems;

    private Context context;
    private LayoutInflater inflater;

    public base_sql_activity_adapter(Context c,ArrayList<SparseArray> all){
        allItems = all;
        context = c;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return allItems.size();
    }

    @Override
    public Object getItem(int position) {
        return allItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return allItems.indexOf(allItems.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        card = allItems.get(position);

        if (convertView==null){
            view = inflater.inflate(R.layout.cell_for_base_sql,parent,false);
        }else{
            view = convertView;
        }

        TextView familia = (TextView) view.findViewById(R.id.text_familia);
        TextView imya = (TextView)view.findViewById(R.id.text_imya);
        TextView otchestvo = (TextView)view.findViewById(R.id.otchestvo);
        TextView kafedra = (TextView)view.findViewById(R.id.kafedra);
        ImageView image = (ImageView)view.findViewById(R.id.image_sql);

        familia.setText(card.get(0));
        imya.setText(card.get(1));
        otchestvo.setText(card.get(2));
        kafedra.setText(card.get(3));
        if (card.get(4).equals("Ж")){
            image.setBackgroundResource(R.drawable.person_female);
        }else{
            image.setBackgroundResource(R.drawable.person_male);
        }

        int gridHeight = base_sql_activity.gridView.getHeight();
        int buttonHeight = gridHeight/6;
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,buttonHeight));
        return view;
    }

}
