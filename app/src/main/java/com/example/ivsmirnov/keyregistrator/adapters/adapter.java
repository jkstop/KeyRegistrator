package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;

import java.sql.Time;
import java.util.ArrayList;

public class adapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> aud;
    private final ArrayList<String> name;
    private final ArrayList<Long> time;
    private final ArrayList<Long> timePut;
    TextView textAud,textName,textTime,textTimePut;


    public adapter(Context context, ArrayList<String> aud,ArrayList<String> name,ArrayList<Long> time,ArrayList<Long> timePut) {
        super(context, R.layout.list,aud);
        this.context = context;
        this.aud = aud;
        this.name = name;
        this.time = time;
        this.timePut = timePut;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list,parent,false);
        textAud = (TextView)rowView.findViewById(R.id.auditoryName);
        textName = (TextView)rowView.findViewById(R.id.teacherName);
        textTime = (TextView)rowView.findViewById(R.id.time);
        textTimePut = (TextView)rowView.findViewById(R.id.timePut);


            if (aud.get(position)!=null&&aud.get(position).equals("_")){
                textAud.setText(" ");
            }else{
                textAud.setText(aud.get(position));
            }


        textName.setText(name.get(position));

        if (time.get(position)==1){
            textTime.setText("");
            textName.setGravity(Gravity.CENTER);
        }else{
            textTime.setText(String.valueOf(new Time(time.get(position))));
        }
        if (timePut.get(position)==0){
            textTimePut.setText("Не сдал");
        }else if (timePut.get(position)==1){
            textTimePut.setText("");
        }else{
            textTimePut.setText(String.valueOf(new Time(timePut.get(position))));
        }

        return rowView;
    }



    public String getString(){

        String string = textAud.getText()+" "+textName.getText()+" "+textTime.getText()+"    "+textTimePut.getText();
        return string;
    }
}
