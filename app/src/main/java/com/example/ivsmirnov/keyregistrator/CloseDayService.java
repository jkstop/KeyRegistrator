package com.example.ivsmirnov.keyregistrator;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;

public class CloseDayService extends Service {

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(context,"Закрываемся...",Toast.LENGTH_SHORT).show();

        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String path = preferences.getString(Values.PATH_FOR_COPY_ON_PC, Environment.getExternalStorageDirectory().getPath());
        String srFileJournal = mPath + "/Journal.txt";
        String dtFileJournal = path + "/Journal.txt";
        String srFileTeachers = mPath + "/Teachers.txt";
        String dtFileTeachers = path + "/Teachers.txt";

        DataBases db = new DataBases(context);
        db.closeDay();
        db.writeFile(Values.WRITE_JOURNAL);
        db.writeFile(Values.WRITE_TEACHERS);
        db.closeDBconnection();

        DataBases.copyfile(context, srFileJournal, dtFileJournal);
        DataBases.copyfile(context, srFileTeachers, dtFileTeachers);

        Intent startCloseDay = new Intent(getApplicationContext(),CloseDayDialog.class);
        startCloseDay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(startCloseDay);

        editor.putBoolean(Values.ALARM_SET,false);
        editor.commit();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
