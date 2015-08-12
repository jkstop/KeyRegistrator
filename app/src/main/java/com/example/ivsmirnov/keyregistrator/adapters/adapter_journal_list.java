package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;

import java.sql.Time;
import java.util.ArrayList;

public class adapter_journal_list extends ArrayAdapter<SparseArray> {

    private final Context context;
    private ArrayList<SparseArray> items;
    private SparseArray <String> card;
    private SparseArray<String> aud;
    private SparseArray<String> name;
    private SparseArray<Long> time;
    private SparseArray<Long> timePut;
    TextView textAud,textName,textTime,textTimePut;


    public adapter_journal_list(Context context, ArrayList<SparseArray> i) {
        super(context, R.layout.list,i);
        this.context = context;
        this.items = i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        card = items.get(position);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list,parent,false);
        textAud = (TextView)rowView.findViewById(R.id.auditoryName);
        textName = (TextView)rowView.findViewById(R.id.teacherName);
        textTime = (TextView)rowView.findViewById(R.id.time);
        textTimePut = (TextView)rowView.findViewById(R.id.timePut);


        if (card.get(0)!=null&&card.get(0).equals("_")){
            textAud.setText(" ");
        }else{
            textAud.setText(card.get(0));
        }


        textName.setText(card.get(1));

        if (Long.parseLong(card.get(2))==1){
            textTime.setText("");
            textName.setGravity(Gravity.CENTER);
        }else{
            textTime.setText(String.valueOf(new Time(Long.parseLong(card.get(2)))));
        }
        if (Long.parseLong(card.get(3))==0){
            textTimePut.setText("Не сдал");
        }else if (Long.parseLong(card.get(3))==1){
            textTimePut.setText("");
        }else{
            textTimePut.setText(String.valueOf(new Time(Long.parseLong(card.get(3)))));
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public String getString(){
        return textAud.getText() + " " + textName.getText() + " " + textTime.getText() + "    " + textTimePut.getText();
    }
}
