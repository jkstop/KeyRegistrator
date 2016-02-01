package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;

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
        return mPreferences.getString(Values.ACTIVE_ACCOUNT_ID, "localAccount");
    }

    public void setDisclaimerWeight(int weight){
        mPreferencesEditor.putInt(Values.DISCLAIMER_SIZE, weight).apply();
    }

    public int getDisclaimerWeight(){
        return mPreferences.getInt(Values.DISCLAIMER_SIZE,30);
    }


    public void setAuditroomColumnsCount(int columns){
        mPreferencesEditor.putInt(Values.COLUMNS_AUD_COUNT, columns).apply();
    }
    public int getAuditroomColumnsCount(){
        return mPreferences.getInt(Values.COLUMNS_AUD_COUNT, 3);
    }

    public ServerConnectionItem getServerConnectionParams(){
        return new ServerConnectionItem()
                .setServerName(mPreferences.getString(Values.SQL_SERVER,""))
                .setUserName(mPreferences.getString(Values.SQL_USER, ""))
                .setUserPassword(mPreferences.getString(Values.SQL_PASSWORD, ""));
    }

    public void setServerConnectionParams(ServerConnectionItem serverConnectionItem){
        mPreferencesEditor.putString(Values.SQL_SERVER, serverConnectionItem.getServerName());
        mPreferencesEditor.putString(Values.SQL_USER, serverConnectionItem.getUserName());
        mPreferencesEditor.putString(Values.SQL_PASSWORD, serverConnectionItem.getUserPassword());
        mPreferencesEditor.apply();
    }

    public boolean getServerStatus(){
        return mPreferences.getBoolean(Values.SQL_STATUS,false);
    }

    public void setServerStatus(boolean status){
        mPreferencesEditor.putBoolean(Values.SQL_STATUS,status).apply();
    }


    //Backup locations
    public void setJournalBackupLocation(String path){
        mPreferencesEditor.putString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL,path).apply();
    }

    public String getJournalBackupLocation(){
        return mPreferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }

    public void setPersonsBackupLocation(String path){
        mPreferencesEditor.putString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS, path).apply();
    }

    public String getPersonsBackupLocation(){
        return mPreferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
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
