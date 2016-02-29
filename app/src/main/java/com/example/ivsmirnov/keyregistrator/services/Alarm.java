package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.async_tasks.Load_from_server;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.util.Calendar;

/**
 * Created by ivsmirnov on 27.02.2016.
 */
public class Alarm {

    private AlarmManager mAlarmManager;
    private PendingIntent mStartAlarmIntent;
    private Context mContext;
    private Settings mSettings;

    public Alarm(Context context){
        this.mContext = context;
        mSettings = new Settings(mContext);
    }

    public void setAlarm(long closingTime){

        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mStartAlarmIntent = PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, closingTime, mStartAlarmIntent);
        }else{
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, closingTime, mStartAlarmIntent);
        }

    }


    public void cancelAlarm(){

        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mStartAlarmIntent = PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.cancel(mStartAlarmIntent);
        mStartAlarmIntent.cancel();
    }

    public boolean isAlarmSet(){
        return (PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_NO_CREATE) != null);
    }

}
