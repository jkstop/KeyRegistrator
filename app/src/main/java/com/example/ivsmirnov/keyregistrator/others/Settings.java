package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ivsmirnov on 30.01.2016.
 */
public class Settings {

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferencesEditor;

    public Settings (Context context){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    }

    public String getActiveAccountID(){
        return mPreferences.getString(Values.ACTIVE_ACCOUNT_ID," ");
    }


    //Auto close status
    public void setAutoCloseStatus(boolean status){
        mPreferencesEditor.putBoolean(Values.ALARM_SET, status).apply();
    }

    public boolean getAutoCloseStatus(){
        return mPreferences.getBoolean(Values.ALARM_SET,false);
    }

    public void cleanAutoCloseStatus(){
        mPreferencesEditor.remove(Values.ALARM_SET).apply();
    }
}
