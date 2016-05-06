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
import java.util.concurrent.ExecutionException;

/**
 * Соединение с сервером
 */
public class SQL_Connection extends AsyncTask<Void,Void,Connection> {

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

    private ServerConnectionItem mServerConnectionItem;
    private Callback mCallback;
    private static SQL_Connection mConnectTask;

    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "KeyRegistratorBase";

    public SQL_Connection (ServerConnectionItem serverConnectionItem, Callback callback){
        this.mServerConnectionItem = serverConnectionItem;
        this.mCallback = callback;
    }

    public static Connection getConnection(Callback callback){
        System.out.println("SQL CONNECT " + SQLconnect);
        if (SQLconnect!=null){
            return SQLconnect;
        } else {
            ServerConnectionItem serverConnectionItem = Settings.getServerConnectionParams();
            if (mConnectTask == null){
                try {
                    mConnectTask = new SQL_Connection(serverConnectionItem, callback);
                    mConnectTask.execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
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
    protected Connection doInBackground(Void... params) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(classs);
            String ConnURL = "jdbc:jtds:sqlserver://" + mServerConnectionItem.getServerName() + ";"
                    + "database=" + db +";user=" + mServerConnectionItem.getUserName() + ";password="
                    + mServerConnectionItem.getUserPassword() + ";";
            SQLconnect = DriverManager.getConnection(ConnURL);
            return SQLconnect;
        }catch (Exception e) {
            e.printStackTrace();
            if (mCallback!=null) mCallback.onServerConnectException(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Connection connection) {
        System.out.println("********** SQL connection END **********");
        if (connection == null){
            Settings.setServerStatus(false);
            System.out.println("SERVER DISCONNECTED");
            if (mCallback!=null) mCallback.onServerDisconnected();
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
        void onServerDisconnected();
        void onServerConnectException(Exception e);
    }
}
