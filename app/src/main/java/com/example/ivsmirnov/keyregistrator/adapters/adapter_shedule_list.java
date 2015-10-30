package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 30.10.2015.
 */
public class adapter_shedule_list extends ArrayAdapter<SparseArray> {

    private Context mContext;
    private ArrayList<SparseArray> mItems;
    private SparseArray<String> card;

    public adapter_shedule_list(Context context, ArrayList<SparseArray> i) {
        super(context, R.layout.row_for_shedule,i);
        this.mContext = context;
        this.mItems = i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        card = mItems.get(position);

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_for_shedule,parent,false);

        TextView timeStart = (TextView)rowView.findViewById(R.id.shedule_timeStart);
        TextView timeEnd = (TextView)rowView.findViewById(R.id.shedule_timeEnd);
        TextView groupName = (TextView)rowView.findViewById(R.id.shedule_groupName);
        TextView teacherName = (TextView)rowView.findViewById(R.id.shedule_teacherName);
        TextView auditroomName = (TextView)rowView.findViewById(R.id.shedule_auditroomName);
        TextView subject = (TextView)rowView.findViewById(R.id.shedule_subject);

        timeStart.setText(card.get(0));
        timeEnd.setText(card.get(1));
        groupName.setText(card.get(2));
        teacherName.setText(card.get(3));
        auditroomName.setText(card.get(4));
        subject.setText(card.get(5));

        return rowView;
    }
}
