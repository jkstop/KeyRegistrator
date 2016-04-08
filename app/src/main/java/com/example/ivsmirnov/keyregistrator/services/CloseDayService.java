package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseDayInterface;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;

import java.util.Calendar;

public class CloseDayService extends Service implements CloseDayInterface {

    private Context context;
    private Alarm mAlarm;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        mAlarm = new Alarm(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            //закрываем открытые помещения
            if (Settings.getAutoCloseStatus()) Settings.setAutoClosedRoomsCount(RoomDB.closeAllRooms());

            //запись файлов
            if (Settings.getFileWriterStatus()){
                if (Settings.getWriteJournalStatus()) new FileWriter(context, FileWriter.WRITE_JOURNAL, false).execute();
                if (Settings.getWriteTeachersStatus()) new FileWriter(context, FileWriter.WRITE_TEACHERS, false).execute();
            }

            //запись на сервер
            if (Settings.getWriteServerStatus()){
                if (Settings.getWriteJournalServerStatus()) new ServerWriter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.JOURNAL_ALL);
                if (Settings.getWriteTeachersServerStatus()) new ServerWriter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.PERSON_ALL);
                if (Settings.getWriteRoomsServerStatus()) new ServerWriter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.ROOMS_ALL);
            }

            //рассылка email
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                if (Settings.getEmailDistributionStatus()){
                    new Send_Email(context, Send_Email.DIALOG_DISABLED).execute(new MailParams()
                            .setTheme(Settings.getMessageTheme())
                            .setBody(Settings.getMessageBody())
                            .setAttachments(Settings.getAttachments())
                            .setRecepients(Settings.getRecepients()));
                }
            }

            //установка планировщика
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
                .putExtra(CloseDay.AUTO_CLOSE_ROOMS, Settings.getAutoClosedRoomsCount())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
