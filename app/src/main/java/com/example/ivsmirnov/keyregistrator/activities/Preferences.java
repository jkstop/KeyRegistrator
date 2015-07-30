package com.example.ivsmirnov.keyregistrator.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 17.06.2015.
 */
public class Preferences extends FragmentActivity {

    public static Handler handler;
    private static Context context;
    public ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        context = this;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id){
            case Values.DIALOG_LOADING:
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("WAIT");
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }
}
