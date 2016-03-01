package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;


public class CloseDay extends AppCompatActivity {

    public static final String AUTO_CLOSE_ROOMS = "auto_close_rooms";

    private Context mContext;
    private DataBaseJournal mDataBaseJournal;
    private DataBaseFavorite mDataBaseFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_close_day);

        mContext = this;

        if (Launcher.mDataBaseJournal!=null){
            mDataBaseJournal = Launcher.mDataBaseJournal;
        } else {
            mDataBaseJournal = new DataBaseJournal(mContext);
        }

        if (Launcher.mDataBaseFavorite!=null){
            mDataBaseFavorite = Launcher.mDataBaseFavorite;
        } else {
            mDataBaseFavorite = new DataBaseFavorite(mContext);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.layout_close_day_text_head);
        if (toolbar!=null){
            setSupportActionBar(toolbar);
        }

        View actionBarView = getLayoutInflater().inflate(R.layout.action_bar_close_day, null);

        TextView textTitle = (TextView)actionBarView.findViewById(R.id.action_bar_close_day_title);

        if (getSupportActionBar()!=null){
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        }

        ((TextView) findViewById(R.id.layout_close_day_card_stat_head)).setText(Values.showDate());

        ((TextView) findViewById(R.id.layout_close_day_card_stat_value_today)).setText(String.valueOf(mDataBaseJournal.getItemCount(DataBaseJournal.COUNT_TODAY)));

        ((TextView) findViewById(R.id.layout_close_day_card_stat_value_journal_items)).setText(String.valueOf(mDataBaseJournal.getItemCount(DataBaseJournal.COUNT_TOTAL)));

        ((TextView) findViewById(R.id.layout_close_day_card_stat_value_person_items)).setText(String.valueOf(mDataBaseFavorite.getPersonsCount()));


        TextView textAutoCloseCount = (TextView)findViewById(R.id.layout_close_day_card_stat_value_autoclose);

        findViewById(R.id.layout_close_day_button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        int titleType = getIntent().getIntExtra(AUTO_CLOSE_ROOMS, -1);
        if (titleType == -1){
            textTitle.setText(R.string.card_statistic_head_stat);
        } else {
            textTitle.setText(R.string.card_statistic_head_close);
            textTitle.setTextColor(getResources().getColor(R.color.colorAccent));
            textAutoCloseCount.setText(String.valueOf(new Settings(mContext).getAutoClosedRoomsCount()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
