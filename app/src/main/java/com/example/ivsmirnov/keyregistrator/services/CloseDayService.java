package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;

public class CloseDayService extends Service implements SQL_Connection.Callback {

    private Context context;


    @Override
    public void onCreate() {
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            ArrayList<String> selectedTasks = SharedPrefs.getShedulerItems();
            String[] allTasks = App.getAppContext().getResources().getStringArray(R.array.shared_preferences_local_tasks_entries);

            //закрываем открытые помещения
            if (selectedTasks.contains(allTasks[0])){
                SharedPrefs.setAutoClosedRoomsCount(RoomDB.closeAllRooms());
            }

            //запись файлов
            if (selectedTasks.contains(allTasks[1])){
                new FileWriter(null, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, SharedPrefs.getBackupItems());
            }

            //запись на сервер
            if (selectedTasks.contains(allTasks[2])){
                SQL_Connection.getConnection(null, 0, this);
            }

            //рассылка email
            if (selectedTasks.contains(allTasks[3])){
                if (SharedPrefs.getEmailPeriods().contains(
                            String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))){
                        new Send_Email(context, Send_Email.DIALOG_DISABLED).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    }
            }

            //установка планировщика
            Alarm.setAlarm(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY);

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            startActivity(new Intent(getApplicationContext(), CloseDay.class)
                    .putExtra(CloseDay.TITLE, CloseDay.CLOSE_TITLE)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        return START_NOT_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        new ServerWriter(ServerWriter.UPDATE_ALL, null, false, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
    }

    @Override
    public void onServerConnectException(Exception e) {

    }
}
