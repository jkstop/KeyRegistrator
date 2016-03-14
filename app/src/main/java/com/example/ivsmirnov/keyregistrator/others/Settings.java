package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Created by ivsmirnov on 30.01.2016.
 */
public class Settings {

    public static final String FREE_USERS = "free_users";

    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mPreferencesEditor;

    public Settings (){

        mPreferences = getPreferences();
        mPreferencesEditor = getPreferencesEditor();
    }

    private static SharedPreferences getPreferences(){
        if (mPreferences == null){
            mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        }
        return mPreferences;
    }

    private static SharedPreferences.Editor getPreferencesEditor(){
        if (mPreferencesEditor == null){
            mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).edit();
        }
        return mPreferencesEditor;
    }

    public static void setMessageTheme (String theme){

        getPreferencesEditor().putString(Values.MAIL_THEME, theme).apply();
    }

    public static String getMessageTheme(){

        return getPreferences().getString(Values.MAIL_THEME, null);
    }

    public static void setMessageBody(String body){
       getPreferencesEditor().putString(Values.MAIL_BODY, body).apply();
    }

    public static String getMessageBody(){

        return getPreferences().getString(Values.MAIL_BODY, null);
    }

    public static void setFreeUsers(ArrayList<String> tags){

        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(tags);
        getPreferencesEditor().putStringSet(FREE_USERS, stringSet).apply();
    }

    public static void addFreeUser(String tag){
        ArrayList <String> freeUsers = getFreeUsers();
        if (!freeUsers.contains(tag)) freeUsers.add(tag);
        setFreeUsers(freeUsers);
    }

    public static void deleteFreeUser(String tag){
        ArrayList <String> freeUsers = getFreeUsers();
        freeUsers.remove(tag);
        setFreeUsers(freeUsers);
    }

    public static ArrayList<String> getFreeUsers(){
        ArrayList<String>items = new ArrayList<>();
        Set<String> freeUsers = getPreferences().getStringSet(FREE_USERS,null);
        if (freeUsers!=null){
            items.addAll(freeUsers);
        }
        return items;
    }

    public static void setAttachments(ArrayList<String>attachments){
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(attachments);
        getPreferencesEditor().putStringSet(Values.MAIL_ATTACHMENTS,stringSet).apply();
    }

    public static ArrayList<String> getAttachments(){
        ArrayList<String> items = new ArrayList<>();
        Set<String> attach = getPreferences().getStringSet(Values.MAIL_ATTACHMENTS,null);
        if (attach!=null){
            items.addAll(attach);
        }
        return items;
    }

    public static void setRecepients(ArrayList<String> recepients){
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(recepients);
        getPreferencesEditor().putStringSet(Values.MAIL_RECEPIENTS, stringSet).apply();
    }

    public static ArrayList<String> getRecepients(){
        ArrayList<String> items = new ArrayList<>();
        Set<String> sets = getPreferences().getStringSet(Values.MAIL_RECEPIENTS, null);
        if (sets!=null){
            items.addAll(sets);
        }
        return items;
    }

    public static void setLastClickedAuditroom(String auditroom){
       getPreferencesEditor().putString(Values.AUDITROOM, auditroom).apply();
    }

    public static String getLastClickedAuditroom(){

        return getPreferences().getString(Values.AUDITROOM, Values.EMPTY);
    }

    public static void setTotalJournalCount(int count){
        getPreferencesEditor().putInt(Values.TOTAL_JOURNAL_COUNT,count).apply();
    }

    public static int getTotalJournalCount(){
        return getPreferences().getInt(Values.TOTAL_JOURNAL_COUNT, 0);
    }

    public static void setActiveAccountID(String accountID){
        getPreferencesEditor().putString(Values.ACTIVE_ACCOUNT_ID, accountID).apply();
    }

    public static String getActiveAccountID(){

        return getPreferences().getString(Values.ACTIVE_ACCOUNT_ID, "localAccount");
    }

    public static void setDisclaimerWeight(int weight){
        getPreferencesEditor().putInt(Values.DISCLAIMER_SIZE, weight).apply();
    }

    public static int getDisclaimerWeight(){

        return getPreferences().getInt(Values.DISCLAIMER_SIZE,30);
    }


    public static void setAuditroomColumnsCount(int columns){
        getPreferencesEditor().putInt(Values.COLUMNS_AUD_COUNT, columns).apply();
    }
    public static int getAuditroomColumnsCount(){

        return getPreferences().getInt(Values.COLUMNS_AUD_COUNT, 3);
    }

    public static ServerConnectionItem getServerConnectionParams(){

        return new ServerConnectionItem()
                .setServerName(getPreferences().getString(Values.SQL_SERVER,""))
                .setUserName(getPreferences().getString(Values.SQL_USER, ""))
                .setUserPassword(getPreferences().getString(Values.SQL_PASSWORD, ""));
    }

    public static void setServerConnectionParams(ServerConnectionItem serverConnectionItem){

        getPreferencesEditor().putString(Values.SQL_SERVER, serverConnectionItem.getServerName());
        getPreferencesEditor().putString(Values.SQL_USER, serverConnectionItem.getUserName());
        getPreferencesEditor().putString(Values.SQL_PASSWORD, serverConnectionItem.getUserPassword());
        getPreferencesEditor().apply();
    }

    public static boolean getServerStatus(){
        return getPreferences().getBoolean(Values.SQL_STATUS,false);
    }

    public static void setServerStatus(boolean status){
        getPreferencesEditor().putBoolean(Values.SQL_STATUS,status).apply();
    }


    //Backup locations
    public static void setJournalBackupLocation(String path){
        getPreferencesEditor().putString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL,path).apply();
    }

    public static String getJournalBackupLocation(){

        return getPreferences().getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }

    public static void setPersonsBackupLocation(String path){
        getPreferencesEditor().putString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS, path).apply();
    }

    public static String getPersonsBackupLocation(){

        return getPreferences().getString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }


    //Auto close
   // public void setAutoCloseStatus(boolean status){
   //     mPreferencesEditor.putBoolean(Values.ALARM_SET, status).apply();
   // }

    public static void setAutoClosedRoomsCount(int closedRoomsCount){
        getPreferencesEditor().putInt(Values.AUTO_CLOSED_COUNT, closedRoomsCount).apply();
    }

    public static int getAutoClosedRoomsCount(){

        return getPreferences().getInt(Values.AUTO_CLOSED_COUNT, 0);
    }

//   // public boolean getAutoCloseStatus(){
       // return mPreferences.getBoolean(Values.ALARM_SET,false);
   // }

 //   public void cleanAutoCloseStatus(){
       // mPreferencesEditor.remove(Values.ALARM_SET).apply();
  //  }
}
