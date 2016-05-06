package com.example.ivsmirnov.keyregistrator.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.ivsmirnov.keyregistrator.R;

import com.example.ivsmirnov.keyregistrator.fragments.SettingsFr;

/**
 * Настройки
 */
public class Preferences extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_preference);

        Toolbar toolbar = (Toolbar)findViewById(R.id.layout_main_app_bar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.layout_main_content_frame,new SettingsFr()).commit();
    }


}
