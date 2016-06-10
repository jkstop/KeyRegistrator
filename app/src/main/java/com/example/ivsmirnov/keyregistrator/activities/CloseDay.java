package com.example.ivsmirnov.keyregistrator.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.fragments.Statistic;


public class CloseDay extends AppCompatActivity {

    public static final String TITLE = "title";
    public static final int STAT_TITLE = 100;
    public static final int CLOSE_TITLE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.layout_main_app_bar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        switch (getIntent().getIntExtra(TITLE, 0)) {
            case STAT_TITLE:
                break;
            case CLOSE_TITLE:
                getSupportActionBar().setTitle(getString(R.string.title_statistic_end));
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
                break;
            default:
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.layout_main_content_frame, Statistic.newInstance(), "statistics").commit();

    }
}
