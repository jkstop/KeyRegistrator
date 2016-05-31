package com.example.ivsmirnov.keyregistrator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ivsmirnov on 26.05.2016.
 */
public class StatFr extends Fragment {

    public static StatFr newInstance (){
        return new StatFr();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics,container,false);

        ((TextView) rootView.findViewById(R.id.close_day_date)).setText(Settings.showDate());

        ((TextView) rootView.findViewById(R.id.close_day_visit_count)).setText(String.valueOf(JournalDB.getItemCount(JournalDB.COUNT_TODAY)));

        ((TextView) rootView.findViewById(R.id.close_day_closed_count)).setText(String.valueOf(Settings.getAutoClosedRoomsCount()));

        ((TextView) rootView.findViewById(R.id.close_day_journal_count)).setText(String.valueOf(JournalDB.getItemCount(JournalDB.COUNT_TOTAL)));

        ((TextView) rootView.findViewById(R.id.close_day_persons_count)).setText(String.valueOf(FavoriteDB.getPersonsCount()));

        return rootView;
    }


}
