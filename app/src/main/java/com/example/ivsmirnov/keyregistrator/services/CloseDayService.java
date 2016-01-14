package com.example.ivsmirnov.keyregistrator.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.async_tasks.Close_day_task;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;

import java.util.Calendar;

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
        String path = preferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
        String srFileJournal = mPath + "/Journal.txt";
        String dtFileJournal = path + "/Journal.txt";
        String srFileTeachers = mPath + "/Teachers.csv";
        String dtFileTeachers = path + "/Teachers.csv";

        DataBaseJournal dbJournal = new DataBaseJournal(context);
        //dbJournal.closeDay();
        dbJournal.closeDB();

        new Close_day_task(context).execute();

        try {
            Values.copyfile(context, srFileJournal, dtFileJournal);
            Values.copyfile(context, srFileTeachers, dtFileTeachers);
        }catch (Exception e){
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
        }

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            Send_Email send_email = new Send_Email(context, new String[]{preferences.getString(Values.EMAIL + "@gmail.com", ""),
                    preferences.getString(Values.PASSWORD, ""),
                    preferences.getString(Values.RECIPIENTS, ""),
                    preferences.getString(Values.BODY, ""),
                    preferences.getString(Values.THEME, "")});
            send_email.execute();
        }

        Intent startCloseDay = new Intent(getApplicationContext(), CloseDay.class);
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
