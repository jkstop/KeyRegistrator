package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.mail_sender.GMailSender;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.File;

import javax.mail.AuthenticationFailedException;

import javax.mail.AuthenticationFailedException;

/**
 * Created by IVSmirnov on 26.08.2015.
 */
public class Send_Email extends AsyncTask<Void, Void, Void> {

    private String[] items;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public Send_Email(Context c, String[] i) {
        this.mContext = c;
        this.items = i;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
    }

    @Override
    protected Void doInBackground(Void... params) {

        File[] attachments;
        if (mSharedPreferences.getBoolean(Values.CHECK_JOURNAL, false) && mSharedPreferences.getBoolean(Values.CHECK_TEACHERS, false)) {
            attachments = new File[2];
            attachments[0] = new File(Environment.getExternalStorageDirectory().getPath() + "/Journal.txt");
            attachments[1] = new File(Environment.getExternalStorageDirectory().getPath() + "/Teachers.csv");
        } else if (mSharedPreferences.getBoolean(Values.CHECK_JOURNAL, false) && !mSharedPreferences.getBoolean(Values.CHECK_TEACHERS, false)) {
            attachments = new File[1];
            attachments[0] = new File(Environment.getExternalStorageDirectory().getPath() + "/Journal.txt");
        } else if (!mSharedPreferences.getBoolean(Values.CHECK_JOURNAL, false) && mSharedPreferences.getBoolean(Values.CHECK_TEACHERS, false)) {
            attachments = new File[1];
            attachments[0] = new File(Environment.getExternalStorageDirectory().getPath() + "/Teachers.csv");
        } else {
            attachments = new File[0];
        }

        GMailSender sender = new GMailSender(items[0], items[1]);
        try {
            sender.sendMail(items[4],
                    items[3] + DataBases.showDate(),
                    items[1],
                    items[2],
                    attachments);
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            Log.d("auth", "error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
