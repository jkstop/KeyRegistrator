package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public void setMessageTheme (String theme){
        mPreferencesEditor.putString(Values.MAIL_THEME, theme).apply();
    }

    public String getMessageTheme(){
        return mPreferences.getString(Values.MAIL_THEME, null);
    }

    public void setMessageBody(String body){
        mPreferencesEditor.putString(Values.MAIL_BODY, body).apply();
    }

    public String getMessageBody(){
        return mPreferences.getString(Values.MAIL_BODY, null);
    }

    public void setAttachments(ArrayList<String>attachments){
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(attachments);
        mPreferencesEditor.putStringSet(Values.MAIL_ATTACHMENTS,stringSet).apply();
    }

    public ArrayList<String> getAttachments(){
        ArrayList<String> items = new ArrayList<>();
        Set<String> attach = mPreferences.getStringSet(Values.MAIL_ATTACHMENTS,null);
        if (attach!=null){
            items.addAll(attach);
        }
        return items;
    }

    public void setRecepients(ArrayList<String> recepients){
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(recepients);
        mPreferencesEditor.putStringSet(Values.MAIL_RECEPIENTS, stringSet).apply();
    }

    public ArrayList<String> getRecepients(){
        ArrayList<String> items = new ArrayList<>();
        Set<String> sets = mPreferences.getStringSet(Values.MAIL_RECEPIENTS, null);
        if (sets!=null){
            items.addAll(sets);
        }
        return items;
    }

    public void setAuthToken(String token){
        mPreferencesEditor.putString(Values.AUTH_TOKEN,token).apply();
    }

    public String getAuthToken(){
        return mPreferences.getString(Values.AUTH_TOKEN,Values.EMPTY);
    }

    public void setLastClickedAuditroom(String auditroom){
        mPreferencesEditor.putString(Values.AUDITROOM, auditroom).apply();
    }

    public String getLastClickedAuditroom(){
        return mPreferences.getString(Values.AUDITROOM, Values.EMPTY);
    }

    public void setTotalJournalCount(int count){
        mPreferencesEditor.putInt(Values.TOTAL_JOURNAL_COUNT,count).apply();
    }

    public int getTotalJournalCount(){
        return mPreferences.getInt(Values.TOTAL_JOURNAL_COUNT, 0);
    }

    public void setActiveAccountID(String accountID){
        mPreferencesEditor.putString(Values.ACTIVE_ACCOUNT_ID, accountID).apply();
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


    //Auto close
    public void setAutoCloseStatus(boolean status){
        mPreferencesEditor.putBoolean(Values.ALARM_SET, status).apply();
    }

    public void setAutoClosedRoomsCount(int closedRoomsCount){
        mPreferencesEditor.putInt(Values.AUTO_CLOSED_COUNT, closedRoomsCount).apply();
    }

    public int getAutoClosedRoomsCount(){
        return mPreferences.getInt(Values.AUTO_CLOSED_COUNT, 0);
    }

    public boolean getAutoCloseStatus(){
        return mPreferences.getBoolean(Values.ALARM_SET,false);
    }

    public void cleanAutoCloseStatus(){
        mPreferencesEditor.remove(Values.ALARM_SET).apply();
    }
}
