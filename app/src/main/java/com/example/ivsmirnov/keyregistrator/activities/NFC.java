package com.example.ivsmirnov.keyregistrator.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.ivsmirnov.keyregistrator.R;

/**
 * Created by ivsmirnov on 03.03.2016.
 */
public class NFC extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nfc_new);

        Toolbar toolbar = (Toolbar)findViewById(R.id.action_bar_nfc);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }
}
