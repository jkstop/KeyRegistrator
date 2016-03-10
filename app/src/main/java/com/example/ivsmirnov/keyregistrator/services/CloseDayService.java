package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.async_tasks.Close_day_task;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseDayInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RoomInterface;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;

import java.util.Calendar;

public class CloseDayService extends Service implements CloseDayInterface {

    private Context context;
    private Settings mSettings;
    private Alarm mAlarm;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        mSettings = new Settings(context);
        mAlarm = new Alarm(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            mSettings.setAutoClosedRoomsCount(Values.closeAllRooms(context));

            //Launcher.mRoomInterface.onRoomClosed();

            new Save_to_file(context, Values.WRITE_JOURNAL, false).execute();
            new Save_to_file(context, Values.WRITE_TEACHERS, false).execute();

            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                new Send_Email(context, Send_Email.DIALOG_DISABLED).execute(new MailParams()
                        .setTheme(mSettings.getMessageTheme())
                        .setBody(mSettings.getMessageBody())
                        .setAttachments(mSettings.getAttachments())
                        .setRecepients(mSettings.getRecepients()));
            }

            mAlarm.setAlarm(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY);

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            onClosed();
        }
        return START_NOT_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onClosed() {

        startActivity(new Intent(getApplicationContext(), CloseDay.class)
                .putExtra(CloseDay.AUTO_CLOSE_ROOMS, mSettings.getAutoClosedRoomsCount())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
