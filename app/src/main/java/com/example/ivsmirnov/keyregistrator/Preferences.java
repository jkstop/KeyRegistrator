package com.example.ivsmirnov.keyregistrator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.LogRecord;

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
