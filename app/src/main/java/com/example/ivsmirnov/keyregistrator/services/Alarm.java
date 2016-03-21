package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Планировщик
 */
public class Alarm {

    private AlarmManager mAlarmManager;
    private PendingIntent mStartAlarmIntent;
    private Context mContext;

    public Alarm(Context context){
        this.mContext = context;
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

    public long closingTime(){
        Calendar now = Calendar.getInstance();
        Calendar when = (Calendar)now.clone();
        when.set(Calendar.HOUR_OF_DAY, 22);
        when.set(Calendar.MINUTE, 1);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);

        if (when.compareTo(now)<=0){
            when.add(Calendar.DATE, 1);
        }
        return when.getTimeInMillis();
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
