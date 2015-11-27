package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;


public class CloseDay_stat_Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_close_day_stat, container,false);
        Context context = root.getContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        DataBaseJournal dbJournal = new DataBaseJournal(context);
        int previousCursorPosition = preferences.getInt(Values.CURSOR_POSITION, dbJournal.cursor.getCount()) +1;

        int nowCursorPosition = dbJournal.cursor.getCount();
        int positionsForToday = nowCursorPosition - previousCursorPosition;

        TextView textDate = (TextView) root.findViewById(R.id.close_day_date);
        TextView textItems = (TextView) root.findViewById(R.id.close_day_items_in_journal);
        TextView textClosed = (TextView) root.findViewById(R.id.close_day_autoclosed_items);
        TextView textTotal = (TextView) root.findViewById(R.id.close_dat_total_items);


        textDate.setText(preferences.getString(Values.TODAY, Values.showDate()));
        textItems.setText(String.valueOf(positionsForToday));
        textClosed.setText(String.valueOf(preferences.getInt(Values.AUTO_CLOSED_COUNT, 0)));
        textTotal.setText(String.valueOf(dbJournal.cursor.getCount()));

        dbJournal.closeDB();
        return root;
    }
}
