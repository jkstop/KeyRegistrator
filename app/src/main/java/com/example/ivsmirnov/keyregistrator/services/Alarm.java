package com.example.ivsmirnov.keyregistrator.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.util.Calendar;

/**
 * Планировщик
 */
public class Alarm {

    private static AlarmManager mAlarmManager;
    private static PendingIntent mStartAlarmIntent;
    //private Context mContext;

    public Alarm(Context context){
        //this.mContext = context;
    }

    public static void setAlarm(long closingTime){
        Context mContext = App.getAppContext();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        mStartAlarmIntent = PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.cancel(mStartAlarmIntent);
        mStartAlarmIntent.cancel();
        System.out.println("ALARM OFF");

        mStartAlarmIntent = PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, closingTime, mStartAlarmIntent);
        }else{
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, closingTime, mStartAlarmIntent);
        }
        System.out.println("ALARM SET");
    }

    public static long getClosingTime(String closingTime){
        Calendar mNowCalendar = Calendar.getInstance();
        Calendar mCloseCalendar = (Calendar)mNowCalendar.clone();

        if (closingTime == null){
            closingTime = SharedPrefs.getShedulerTime();
        }
        String [] timeSplit = closingTime.split(":");
        mCloseCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]));
        mCloseCalendar.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]));
        mCloseCalendar.set(Calendar.SECOND,0);

        //если сегодня пропустили, то перенос на завтра
        if (mCloseCalendar.compareTo(mNowCalendar) <= 0){
            mCloseCalendar.add(Calendar.DATE, 1);
        }

        System.out.println("ALARM TIME " + mCloseCalendar.getTime());

        return mCloseCalendar.getTimeInMillis();
    }



    public static void cancelAlarm(){
        Context mContext = App.getAppContext();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mStartAlarmIntent = PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.cancel(mStartAlarmIntent);
        mStartAlarmIntent.cancel();
        System.out.println("ALARM OFF");
    }

    public static boolean isAlarmSet(){
        Context mContext = App.getAppContext();
        return (PendingIntent.getService(mContext,
                1,
                new Intent(mContext, CloseDayService.class),
                PendingIntent.FLAG_NO_CREATE) != null);
    }

}
