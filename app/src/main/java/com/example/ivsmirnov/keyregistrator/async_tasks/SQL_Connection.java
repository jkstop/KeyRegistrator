package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.example.ivsmirnov.keyregistrator.fragments.DialogPassword;
import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Соединение с сервером
 */
public class SQL_Connection extends AsyncTask<Void,Void,Exception> {

    //таблицы
    public static final String JOURNAL_TABLE = "JOURNAL_V2";
    public static final String PERSONS_TABLE = "PERSONS";
    public static final String ROOMS_TABLE = "ROOMS";
    public static final String ALL_STAFF_TABLE = "STAFF_NEW";

    //колонки
    public static final String COLUMN_JOURNAL_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String COLUMN_JOURNAL_ROOM = "ROOM";
    public static final String COLUMN_JOURNAL_TIME_IN = "TIME_IN";
    public static final String COLUMN_JOURNAL_TIME_OUT = "TIME_OUT";
    public static final String COLUMN_JOURNAL_ACCESS = "ACCESS";
    public static final String COLUMN_JOURNAL_PERSON_INITIALS = "PERSON_INITIALS";
    public static final String COLUMN_JOURNAL_PERSON_TAG = "PERSON_TAG";

    public static final String COLUMN_PERSONS_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String COLUMN_PERSONS_LASTNAME = "LASTNAME";
    public static final String COLUMN_PERSONS_FIRSTNAME = "FIRSTNAME";
    public static final String COLUMN_PERSONS_MIDNAME = "MIDNAME";
    public static final String COLUMN_PERSONS_DIVISION = "DIVISION";
    public static final String COLUMN_PERSONS_TAG = "TAG";
    public static final String COLUMN_PERSONS_SEX = "SEX";
    public static final String COLUMN_PERSONS_PHOTO_BASE64 = "PHOTO_BASE64";

    public static final String COLUMN_ROOMS_ROOM = "ROOM";
    public static final String COLUMN_ROOMS_STATUS = "STATUS";
    public static final String COLUMN_ROOMS_ACCESS = "ACCESS";
    public static final String COLUMN_ROOMS_TIME = "TIME";
    public static final String COLUMN_ROOMS_LAST_VISITER = "LAST_VISITER";
    public static final String COLUMN_ROOMS_RADIO_LABEL = "RADIO_LABEL";
    public static final String COLUMN_ROOMS_PHOTO = "PHOTO";

    public static final String COLUMN_ALL_STAFF_DIVISION = "NAME_DIVISION";
    public static final String COLUMN_ALL_STAFF_POSITION = "NAME_POSITION";
    public static final String COLUMN_ALL_STAFF_LASTNAME = "LASTNAME";
    public static final String COLUMN_ALL_STAFF_FIRSTNAME = "FIRSTNAME";
    public static final String COLUMN_ALL_STAFF_MIDNAME = "MIDNAME";
    public static final String COLUMN_ALL_STAFF_SEX = "SEX";
    public static final String COLUMN_ALL_STAFF_PHOTO = "PHOTO";
    public static final String COLUMN_ALL_STAFF_TAG = "RADIO_LABEL";

    private static Connection SQLconnect;

    private String mServerName;
    private Callback mCallback;
    private static SQL_Connection mConnectTask;

    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "KeyRegistratorBase";

    public SQL_Connection (String serverName, Callback callback){
        this.mServerName = serverName;
        this.mCallback = callback;
    }

    public static Connection getConnection(String serverName, Callback callback){
        System.out.println("SQL CONNECT " + SQLconnect);
        if (serverName !=null){
            new SQL_Connection(serverName, callback).execute();
        } else {
            if (SQLconnect!=null){
                return SQLconnect;
            } else {
                new SQL_Connection(Settings.getServerName(), callback).execute();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("********** SQL connection START **********");
    }

    @Override
    protected Exception doInBackground(Void... params) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(classs);
            String ConnURL = "jdbc:jtds:sqlserver://" + mServerName + ";"
                    + "database=" + db +";user=shsupport;password=podderzhka;";
            SQLconnect = DriverManager.getConnection(ConnURL);
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            SQLconnect = null;
            //if (mCallback!=null) mCallback.onServerConnectException(e);
            return e;
        }
    }

    @Override
    protected void onPostExecute(Exception e) {
        System.out.println("********** SQL connection END **********");
        if (e != null){
            Settings.setServerStatus(false);
            System.out.println("SERVER DISCONNECTED");
            if (mCallback!=null) mCallback.onServerConnectException(e);
            //if (mConnectionInterface!=null) mConnectionInterface.onServerConnectException(e);
            //if (mConnectionInterface!=null) mConnectionInterface.onServerDisconnected();
        } else {
            Settings.setServerStatus(true);
            if (mCallback!=null) mCallback.onServerConnected();
            //if (mConnectionInterface!=null) mConnectionInterface.onServerConnected();
            System.out.println("SERVER CONNECTED");
        }
    }

    public interface Callback{
        void onServerConnected();
        void onServerConnectException(Exception e);
    }
}
