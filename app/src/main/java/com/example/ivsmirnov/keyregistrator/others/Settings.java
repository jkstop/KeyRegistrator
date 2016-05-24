package com.example.ivsmirnov.keyregistrator.others;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Настройки
 */
public class Settings {

    public static final String FREE_USERS = "free_users";
    public static final String COLUMNS_AUD_COUNT = "columns_aud_count";
    public static final String AUDITROOM = "auditroom";
    public static final String AUTO_CLOSED_COUNT = "auto_closed_count";
    public static final String TOTAL_JOURNAL_COUNT = "total_journal_count";
    public static final String PATH_FOR_COPY_ON_PC_FOR_JOURNAL = "path_for_copy_on_pc_for_journal";
    public static final String PATH_FOR_COPY_ON_PC_FOR_TEACHERS = "path_for_copy_on_pc_for_teachers";
    public static final String DISCLAIMER_SIZE = "disclaimer_size";
    public static final String SQL_SERVER = "sql_server";
    public static final String SQL_USER = "sql_user";
    public static final String SQL_PASSWORD = "sql_password";
    public static final String SQL_STATUS = "sql_status";
    public static final String ACTIVE_ACCOUNT_ID = "active_account_id";
    public static final String MAIL_RECEPIENTS = "mail_recepients";
    public static final String MAIL_ATTACHMENTS = "mail_attachments";
    public static final String MAIL_THEME = "mail_theme";
    public static final String MAIL_BODY = "mail_body";
    public static final String SHEDULER_STATUS = getStringFromRes(R.string.shared_preferences_sheduler);
    public static final String AUTO_CLOSE_STATUS = getStringFromRes(R.string.shared_preferences_auto_close);
    public static final String SHEDULER_TIME = getStringFromRes(R.string.shared_preferences_sheduler_time);
    public static final String EMAIL_DISTRIBUTION_STATUS = getStringFromRes(R.string.shared_preferences_email_distribution);
    public static final String FILE_WRITER = getStringFromRes(R.string.shared_preferences_file_writer);
    public static final String FILE_WRITE_JOURNAL = getStringFromRes(R.string.shared_preferences_write_journal);
    public static final String FILE_WRITE_TEACHERS = getStringFromRes(R.string.shared_preferences_write_teachers);
    public static final String FILE_WRITE_ROOMS = getStringFromRes(R.string.shared_preferences_write_rooms);
    public static final String SERVER_WRITER = getStringFromRes(R.string.shared_preferences_write_server);
    public static final String SERVER_WRITE_JOURNAL = getStringFromRes(R.string.shared_preferences_write_journal_server);
    public static final String SERVER_WRITE_TEACHERS = getStringFromRes(R.string.shared_preferences_write_teachers_server);
    public static final String SERVER_WRITE_ROOMS = getStringFromRes(R.string.shared_preferences_write_rooms_server);
    public static final String SERVER_NAME = getStringFromRes(R.string.shared_preferences_sql_settings);
    public static final String last_room_time_in = "timein";
    public static final String COLUMNS_LAND = "COLUMNS_LAND";
    public static final String ROWS_LAND = "ROWS_LAND";
    public static final String COLUMNS_PORT = "COLUMNS_PORT";
    public static final String ROWS_PORT = "ROWS_PORT";

    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mPreferencesEditor;

    private static String getStringFromRes(int strId){
        return App.getAppContext().getResources().getString(strId);
    }

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

    public static void setServerName (String serverName){
        getPreferencesEditor().putString(SERVER_NAME, serverName).apply();
    }

    public static String getServerName(){
        return getPreferences().getString(SERVER_NAME, "");
    }

    public static void setColumnsPortrait (int columnsPortrait){
        getPreferencesEditor().putInt(COLUMNS_PORT, columnsPortrait).apply();
    }

    public static void setRowsPortrait (int rowsPortrait){
        getPreferencesEditor().putInt(ROWS_PORT, rowsPortrait).apply();
    }

    public static int getColumnsPortrait(){
        return getPreferences().getInt(COLUMNS_PORT, 2);
    }

    public static int getRowsPortrait(){
        return getPreferences().getInt(ROWS_PORT, 3);
    }

    public static void setColumnsLandscape (int columnsLandscape){
        getPreferencesEditor().putInt(COLUMNS_LAND, columnsLandscape).apply();
    }

    public static int getColumnsLandscape (){
        return getPreferences().getInt(COLUMNS_LAND, 3);
    }

    public static void setRowsLandscape (int rowsLandscape){
        getPreferencesEditor().putInt(ROWS_LAND, rowsLandscape).apply();
    }

    public static int getRowsLandscape (){
        return getPreferences().getInt(ROWS_LAND, 3);
    }

    public static void setLAstRoomTimeIn(long timeIn){
        getPreferencesEditor().putLong(last_room_time_in, timeIn).apply();
    }

    public static long getLastRoomTimeIn(){
        return getPreferences().getLong(last_room_time_in, 0);
    }

    public static boolean getWriteServerStatus(){
        return getPreferences().getBoolean(SERVER_WRITER, false);
    }

    public static boolean getWriteJournalServerStatus(){
        return getPreferences().getBoolean(SERVER_WRITE_JOURNAL, false);
    }

    public static boolean getWriteTeachersServerStatus(){
        return getPreferences().getBoolean(SERVER_WRITE_TEACHERS, false);
    }

    public static boolean getWriteRoomsServerStatus(){
        return getPreferences().getBoolean(SERVER_WRITE_ROOMS, false);
    }

    public static boolean getShedulerStatus(){
        return getPreferences().getBoolean(SHEDULER_STATUS, false);
    }

    public static boolean getFileWriterStatus(){
        return getPreferences().getBoolean(FILE_WRITER, false);
    }

    public static boolean getWriteJournalStatus(){
        return getPreferences().getBoolean(FILE_WRITE_JOURNAL, false);
    }

    public static boolean getWriteTeachersStatus(){
        return getPreferences().getBoolean(FILE_WRITE_TEACHERS, false);
    }

    public static boolean getWriteRoomsStatus(){
        return getPreferences().getBoolean(FILE_WRITE_ROOMS, false);
    }

    public static boolean getEmailDistributionStatus(){
        return getPreferences().getBoolean(EMAIL_DISTRIBUTION_STATUS,false);
    }

    public static boolean getAutoCloseStatus(){
        return getPreferences().getBoolean(AUTO_CLOSE_STATUS, false);
    }

    public static String getShedulerTime(){
        return getPreferences().getString(SHEDULER_TIME, "00:00");
    }

    public static void setMessageTheme (String theme){

        getPreferencesEditor().putString(MAIL_THEME, theme).apply();
    }

    public static String getMessageTheme(){

        return getPreferences().getString(MAIL_THEME, null);
    }

    public static void setMessageBody(String body){
       getPreferencesEditor().putString(MAIL_BODY, body).apply();
    }

    public static String getMessageBody(){

        return getPreferences().getString(MAIL_BODY, null);
    }

    public static void setAttachments(ArrayList<String>attachments){
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(attachments);
        getPreferencesEditor().putStringSet(MAIL_ATTACHMENTS,stringSet).apply();
    }

    public static ArrayList<String> getAttachments(){
        ArrayList<String> items = new ArrayList<>();
        Set<String> attach = getPreferences().getStringSet(MAIL_ATTACHMENTS,null);
        if (attach!=null){
            items.addAll(attach);
        }
        return items;
    }

    public static void setRecepients(ArrayList<String> recepients){
        Set<String> stringSet = new HashSet<>();
        stringSet.addAll(recepients);
        getPreferencesEditor().putStringSet(MAIL_RECEPIENTS, stringSet).apply();
    }

    public static ArrayList<String> getRecepients(){
        ArrayList<String> items = new ArrayList<>();
        Set<String> sets = getPreferences().getStringSet(MAIL_RECEPIENTS, null);
        if (sets!=null){
            items.addAll(sets);
        }
        return items;
    }

    public static void setLastClickedAuditroom(String auditroom){
       getPreferencesEditor().putString(AUDITROOM, auditroom).apply();
    }

    public static String getLastClickedAuditroom(){

        return getPreferences().getString(AUDITROOM, Values.EMPTY);
    }

    public static void setTotalJournalCount(int count){
        getPreferencesEditor().putInt(TOTAL_JOURNAL_COUNT,count).apply();
    }

    public static int getTotalJournalCount(){
        return getPreferences().getInt(TOTAL_JOURNAL_COUNT, 0);
    }

    public static void setActiveAccountID(String accountID){
        getPreferencesEditor().putString(ACTIVE_ACCOUNT_ID, accountID).apply();
    }

    public static String getActiveAccountID(){

        return getPreferences().getString(ACTIVE_ACCOUNT_ID, "localAccount");
    }

    public static void setDisclaimerWeight(int weight){
        getPreferencesEditor().putInt(DISCLAIMER_SIZE, weight).apply();
    }

    public static int getDisclaimerWeight(){

        return getPreferences().getInt(DISCLAIMER_SIZE,30);
    }


    public static void setAuditroomColumnsCount(int columns){
        getPreferencesEditor().putInt(COLUMNS_AUD_COUNT, columns).apply();
    }
    public static int getAuditroomColumnsCount(){

        return getPreferences().getInt(COLUMNS_AUD_COUNT, 3);
    }

    public static ServerConnectionItem getServerConnectionParams(){

        return new ServerConnectionItem()
                .setServerName(getPreferences().getString(SQL_SERVER,""))
                .setUserName(getPreferences().getString(SQL_USER, ""))
                .setUserPassword(getPreferences().getString(SQL_PASSWORD, ""));
    }

    public static void setServerConnectionParams(ServerConnectionItem serverConnectionItem){

        getPreferencesEditor().putString(SQL_SERVER, serverConnectionItem.getServerName());
        getPreferencesEditor().putString(SQL_USER, serverConnectionItem.getUserName());
        getPreferencesEditor().putString(SQL_PASSWORD, serverConnectionItem.getUserPassword());
        getPreferencesEditor().apply();
    }

    public static boolean getServerStatus(){
        return getPreferences().getBoolean(SQL_STATUS,false);
    }

    public static void setServerStatus(boolean status){
        getPreferencesEditor().putBoolean(SQL_STATUS,status).apply();
    }


    //Backup locations
    public static void setJournalBackupLocation(String path){
        getPreferencesEditor().putString(PATH_FOR_COPY_ON_PC_FOR_JOURNAL,path).apply();
    }

    public static String getJournalBackupLocation(){

        return getPreferences().getString(PATH_FOR_COPY_ON_PC_FOR_JOURNAL,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }

    public static void setPersonsBackupLocation(String path){
        getPreferencesEditor().putString(PATH_FOR_COPY_ON_PC_FOR_TEACHERS, path).apply();
    }

    public static String getPersonsBackupLocation(){

        return getPreferences().getString(PATH_FOR_COPY_ON_PC_FOR_TEACHERS,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }


    //Auto close
   // public void setAutoCloseStatus(boolean status){
   //     mPreferencesEditor.putBoolean(Values.ALARM_SET, status).apply();
   // }

    public static void setAutoClosedRoomsCount(int closedRoomsCount){
        getPreferencesEditor().putInt(AUTO_CLOSED_COUNT, closedRoomsCount).apply();
    }

    public static int getAutoClosedRoomsCount(){

        return getPreferences().getInt(AUTO_CLOSED_COUNT, 0);
    }

//   // public boolean getAutoCloseStatus(){
       // return mPreferences.getBoolean(Values.ALARM_SET,false);
   // }

 //   public void cleanAutoCloseStatus(){
       // mPreferencesEditor.remove(Values.ALARM_SET).apply();
  //  }
}
