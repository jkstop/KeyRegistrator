package com.example.ivsmirnov.keyregistrator.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.fragments.StatFr;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CloseDay extends AppCompatActivity {

    public static final String TITLE = "title";
    public static final int STAT_TITLE = 100;
    public static final int CLOSE_TITLE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_close_day_activity);

        Toolbar mToolbar = (Toolbar)findViewById(R.id.layout_main_app_bar);
        if (mToolbar!=null){
            setSupportActionBar(mToolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        switch (getIntent().getIntExtra(TITLE, 0)){
            case STAT_TITLE:
                break;
            case CLOSE_TITLE:
                getSupportActionBar().setTitle("Занятия завершены!");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
                break;
            default:
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.layout_main_content_frame, StatFr.newInstance(), "statistics").commit();

        /*View actionBarView = View.inflate(this, R.layout.action_bar_close_day, null);

        TextView mTextTitle = (TextView)actionBarView.findViewById(R.id.action_bar_close_day_title);

        if (getSupportActionBar()!=null){
            ActionBar mActionBar = getSupportActionBar();
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        }

        ((TextView) findViewById(R.id.layout_close_day_card_stat_head)).setText(showDate());

        ((TextView) findViewById(R.id.layout_close_day_card_stat_value_today)).setText(String.valueOf(JournalDB.getItemCount(JournalDB.COUNT_TODAY)));

        ((TextView) findViewById(R.id.layout_close_day_card_stat_value_journal_items)).setText(String.valueOf(JournalDB.getItemCount(JournalDB.COUNT_TOTAL)));

        ((TextView) findViewById(R.id.layout_close_day_card_stat_value_person_items)).setText(String.valueOf(FavoriteDB.getPersonsCount()));


        TextView mTextAutoCloseCount = (TextView)findViewById(R.id.layout_close_day_card_stat_value_autoclose);

        findViewById(R.id.layout_close_day_button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        int titleType = getIntent().getIntExtra(AUTO_CLOSE_ROOMS, -1);
        if (titleType == -1){
            mTextTitle.setText(R.string.card_statistic_head_stat);
        } else {
            mTextTitle.setText(R.string.card_statistic_head_close);
            mTextTitle.setTextColor(getResources().getColor(R.color.colorAccent));
            mTextAutoCloseCount.setText(String.valueOf(Settings.getAutoClosedRoomsCount()));
        }*/
    }

    private String showDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy",new Locale("ru"));
        return String.valueOf(dateFormat.format(new Date())) + " г.";
    }

}
