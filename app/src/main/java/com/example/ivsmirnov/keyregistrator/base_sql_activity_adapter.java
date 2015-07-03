package com.example.ivsmirnov.keyregistrator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity_adapter extends BaseAdapter {

    private ArrayList <String> kaf;
    private ArrayList <String> name;
    private ArrayList <String> surname;
    private ArrayList <String> lastname;

    private Context context;
    private LayoutInflater inflater;

    public base_sql_activity_adapter(Context c,ArrayList<String> k,ArrayList<String> i, ArrayList<String> f, ArrayList<String> o){
        kaf = k;
        name = i;
        surname = f;
        lastname = o;
        context = c;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public Object getItem(int position) {
        return name.get(position);
    }

    @Override
    public long getItemId(int position) {
        return name.indexOf(name.get(position));
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView==null){
            view = inflater.inflate(R.layout.cell_for_base_sql,parent,false);
        }else{
            view = convertView;
        }

        TextView familia = (TextView) view.findViewById(R.id.text_familia);
        TextView imya = (TextView)view.findViewById(R.id.text_imya);
        TextView otchestvo = (TextView)view.findViewById(R.id.otchestvo);
        TextView kafedra = (TextView)view.findViewById(R.id.kafedra);



        familia.setText(name.get(position));
        imya.setText(surname.get(position));
        otchestvo.setText(lastname.get(position));
        kafedra.setText(kaf.get(position));
        return view;
    }

}
