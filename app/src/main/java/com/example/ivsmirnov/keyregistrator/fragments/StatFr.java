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


        ((TextView) rootView.findViewById(R.id.layout_close_day_card_stat_head)).setText(showDate());

        ((TextView) rootView.findViewById(R.id.layout_close_day_card_stat_value_today)).setText(String.valueOf(JournalDB.getItemCount(JournalDB.COUNT_TODAY)));

        ((TextView) rootView.findViewById(R.id.layout_close_day_card_stat_value_journal_items)).setText(String.valueOf(JournalDB.getItemCount(JournalDB.COUNT_TOTAL)));

        ((TextView) rootView.findViewById(R.id.layout_close_day_card_stat_value_person_items)).setText(String.valueOf(FavoriteDB.getPersonsCount()));


        TextView mTextAutoCloseCount = (TextView)rootView.findViewById(R.id.layout_close_day_card_stat_value_autoclose);

        //int titleType = getIntent().getIntExtra(AUTO_CLOSE_ROOMS, -1);
        //if (titleType == -1){
        //    mTextTitle.setText(R.string.card_statistic_head_stat);
        //} else {
        //    mTextTitle.setText(R.string.card_statistic_head_close);
        //    mTextTitle.setTextColor(getResources().getColor(R.color.colorAccent));
            mTextAutoCloseCount.setText(String.valueOf(Settings.getAutoClosedRoomsCount()));
        //}

        return rootView;
    }

    private String showDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy",new Locale("ru"));
        return String.valueOf(dateFormat.format(new Date())) + " г.";
    }
}
