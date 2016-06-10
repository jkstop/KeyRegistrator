package com.example.ivsmirnov.keyregistrator.others;

import android.os.Environment;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Настройки
 */
public class SharedPrefs {

    private static final String AUTO_CLOSED_COUNT = "auto_closed_count";
    private static final String ACTIVE_ACCOUNT_ID = "active_account_id";
    private static final String ACTIVE_ACCOUNT_EMAIL = "active_account_email";
    private static final String MAIL_RECEPIENTS = getStringFromRes(R.string.shared_preferences_email_recipients);
    private static final String MAIL_ATTACHMENTS = "mail_attachments";
    private static final String SHEDULER_STATUS = getStringFromRes(R.string.shared_preferences_sheduler);
    private static final String SHEDULER_TIME = getStringFromRes(R.string.shared_preferences_sheduler_time);
    private static final String SERVER_WRITER = getStringFromRes(R.string.shared_preferences_write_server);
    private static final String SERVER_NAME = getStringFromRes(R.string.shared_preferences_sql_settings);
    private static final String SHEDULER_ITEMS = getStringFromRes(R.string.shared_preferences_local_tasks_key);
    private static final String GRID_COLUMNS = "GRID_COLUMNS";
    private static final String GRID_ROWS = "GRID_ROWS";
    private static final String EMAIL_PERIOD = getStringFromRes(R.string.shared_preferences_email_period);
    private static final String BACKUP_ITEMS = getStringFromRes(R.string.shared_preferences_backup_items);
    private static final String BACKUP_LOCATION = getStringFromRes(R.string.shared_preferences_backup_location);

    private static android.content.SharedPreferences mPreferences;
    private static android.content.SharedPreferences.Editor mPreferencesEditor;

    private static String getStringFromRes(int strId){
        return App.getAppContext().getResources().getString(strId);
    }

    public SharedPrefs(){

        mPreferences = getPreferences();
        mPreferencesEditor = getPreferencesEditor();
    }

    private static android.content.SharedPreferences getPreferences(){
        if (mPreferences == null){
            mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        }
        return mPreferences;
    }

    private static android.content.SharedPreferences.Editor getPreferencesEditor(){
        if (mPreferencesEditor == null){
            mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).edit();
        }
        return mPreferencesEditor;
    }

    public static String showDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy",new Locale("ru"));
        return String.valueOf(dateFormat.format(new Date())) + " г.";
    }

    public static void setBackupLocation (String path){
        getPreferencesEditor().putString(BACKUP_LOCATION, path).apply();
    }

    public static String getBackupLocation(){
        return getPreferences().getString(BACKUP_LOCATION, Environment.getExternalStorageDirectory().getPath());
    }

    public static ArrayList<String> getBackupItems(){
        Set<String> items = getPreferences().getStringSet(BACKUP_ITEMS, null);
        if (items!=null){
            return new ArrayList<>(items);
        }else{
            return new ArrayList<>();
        }
    }

    public static ArrayList<String> getShedulerItems(){
        Set<String> items = getPreferences().getStringSet(SHEDULER_ITEMS, null);
        if (items!=null){
            return new ArrayList<>(items);
        } else {
            return new ArrayList<>();
        }
    }

    public static ArrayList<String> getEmailPeriods(){
        Set<String> items = getPreferences().getStringSet(EMAIL_PERIOD, null);
        if (items!=null){
            return new ArrayList<>(items);
        } else {
            return new ArrayList<>();
        }
    }

    public static void setServerName (String serverName){
        getPreferencesEditor().putString(SERVER_NAME, serverName).apply();
    }

    public static String getServerName(){
        return getPreferences().getString(SERVER_NAME, "-");
    }

    public static void setGridColumns (int columnsPortrait){
        getPreferencesEditor().putInt(GRID_COLUMNS, columnsPortrait).apply();
    }

    public static void setGridRows (int rowsPortrait){
        getPreferencesEditor().putInt(GRID_ROWS, rowsPortrait).apply();
    }

    public static int getGridColumns(){
        return getPreferences().getInt(GRID_COLUMNS, 3);
    }

    public static int getGridRows(){
        return getPreferences().getInt(GRID_ROWS, 3);
    }

    public static boolean getWriteServerStatus(){
        return getPreferences().getBoolean(SERVER_WRITER, false);
    }

    public static boolean getShedulerStatus(){
        return getPreferences().getBoolean(SHEDULER_STATUS, false);
    }

    public static String getShedulerTime(){
        return getPreferences().getString(SHEDULER_TIME, "00:00");
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

    public static void setActiveAccountID(String accountID){
        getPreferencesEditor().putString(ACTIVE_ACCOUNT_ID, accountID).apply();
    }

    public static String getActiveAccountID(){
        return getPreferences().getString(ACTIVE_ACCOUNT_ID, App.getAppContext().getString(R.string.local_account));
    }

    public static void setActiveAccountEmail(String accountEmail){
        getPreferencesEditor().putString(ACTIVE_ACCOUNT_EMAIL, accountEmail).apply();
    }

    public static String getActiveAccountEmail(){
        return getPreferences().getString(ACTIVE_ACCOUNT_EMAIL, null);
    }

    public static void setAutoClosedRoomsCount(int closedRoomsCount){
        getPreferencesEditor().putInt(AUTO_CLOSED_COUNT, closedRoomsCount).apply();
    }

    public static int getAutoClosedRoomsCount(){

        return getPreferences().getInt(AUTO_CLOSED_COUNT, 0);
    }

}
