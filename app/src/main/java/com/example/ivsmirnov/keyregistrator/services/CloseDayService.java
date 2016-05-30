package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseDayInterface;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;

import java.util.ArrayList;
import java.util.Calendar;

public class CloseDayService extends Service implements CloseDayInterface {

    private Context context;


    @Override
    public void onCreate() {
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("START SHEDULER");

        try {

            ArrayList<String> selectedTasks = Settings.getShedulerItems();
            String[] allTasks = App.getAppContext().getResources().getStringArray(R.array.shared_preferences_local_tasks_entries);

            //закрываем открытые помещения
            if (selectedTasks.contains(allTasks[0])){
                Settings.setAutoClosedRoomsCount(RoomDB.closeAllRooms());
            }

            //запись файлов
            if (selectedTasks.contains(allTasks[1])){
                new FileWriter(context, FileWriter.WRITE_JOURNAL, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                new FileWriter(context, FileWriter.WRITE_TEACHERS, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                new FileWriter(context, FileWriter.WRITE_ROOMS, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }

            //запись на сервер
            if (selectedTasks.contains(allTasks[2])){
                new ServerWriter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.JOURNAL_UPDATE);
                new ServerWriter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.PERSON_UPDATE);
                new ServerWriter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.ROOMS_UPDATE);
            }

            //рассылка email
            if (selectedTasks.contains(allTasks[3])){

                    if (Settings.getEmailPeriods().contains(
                            String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))){

                        new Send_Email(context, Send_Email.DIALOG_DISABLED).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    }
            }

            //установка планировщика
            //mAlarm.setAlarm(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY);
            Alarm.setAlarm(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY);

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
                .putExtra(CloseDay.TITLE, CloseDay.CLOSE_TITLE)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
