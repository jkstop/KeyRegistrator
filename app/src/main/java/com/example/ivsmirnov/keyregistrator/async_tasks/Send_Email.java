package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.mail_sender.GMailSender;

import java.io.File;

/**
 * Created by IVSmirnov on 26.08.2015.
 */
public class Send_Email extends AsyncTask<Void, Void, Void> {

    String[] items;

    public Send_Email(String[] i) {
        this.items = i;
    }

    @Override
    protected Void doInBackground(Void... params) {

        for (int i = 0; i < items.length; i++) {
            Log.d("items" + i, items[i]);
        }

        GMailSender sender = new GMailSender(items[0], items[1]);
        try {
            sender.sendMail(items[4],
                    items[3] + DataBases.showDate(),
                    items[1],
                    items[2],
                    new File(Environment.getExternalStorageDirectory().getPath() + "/Journal.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
