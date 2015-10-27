package com.example.ivsmirnov.keyregistrator.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;


public class CloseDay extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_day_dialog);
        this.setFinishOnTouchOutside(false);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        TextView textHead = (TextView) findViewById(R.id.text_close);
        int type = getIntent().getIntExtra("type",0);
        if (type==1){
            textHead.setText("Статистика");
            textHead.setTextColor(Color.BLACK);
            editor.remove(Values.AUTO_CLOSED_COUNT);
            editor.commit();
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

    public void okClick(View view) {
        finish();
    }
}
