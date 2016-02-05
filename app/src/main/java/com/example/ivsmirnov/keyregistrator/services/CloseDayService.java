package com.example.ivsmirnov.keyregistrator.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.async_tasks.Close_day_task;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseDayInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;

import java.util.Calendar;

public class CloseDayService extends Service implements CloseDayInterface {

    private Context context;
    private Settings mSettings;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        mSettings = new Settings(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSettings.setAutoClosedRoomsCount(Values.closeAllRooms(context));
        new Close_day_task(context, this).execute();

        /*Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            Send_Email send_email = new Send_Email(context, new String[]{preferences.getString(Values.EMAIL + "@gmail.com", ""),
                    preferences.getString(Values.PASSWORD, ""),
                    preferences.getString(Values.RECIPIENTS, ""),
                    preferences.getString(Values.BODY, ""),
                    preferences.getString(Values.THEME, "")});
            send_email.execute();
        }*/

        mSettings.setAutoCloseStatus(false);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onClosed() {
        startActivity(new Intent(getApplicationContext(), CloseDay.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
