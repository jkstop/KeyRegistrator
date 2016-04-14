package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Соединение с сервером
 */
public class SQL_Connection extends AsyncTask<Void,Void,Exception> {

    //таблицы
    public static final String JOURNAL_TABLE = "JOURNAL";
    public static final String PERSONS_TABLE = "TEACHERS";
    public static final String ROOMS_TABLE = "ROOMS";
    public static final String ALL_STAFF_TABLE = "STAFF_NEW";

    //колонки
    public static final String COLUMN_JOURNAL_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String COLUMN_JOURNAL_AUDITROOM = "AUDITROOM";
    public static final String COLUMN_JOURNAL_TIME_IN = "TIME_IN";
    public static final String COLUMN_JOURNAL_TIME_OUT = "TIME_OUT";
    public static final String COLUMN_JOURNAL_ACCESS = "ACCESS";
    public static final String COLUMN_JOURNAL_LASTNAME = "PERSON_LASTNAME";
    public static final String COLUMN_JOURNAL_FIRSTNAME = "PERSON_FIRSTNAME";
    public static final String COLUMN_JOURNAL_MIDNAME = "PERSON_MIDNAME";
    public static final String COLUMN_JOURNAL_PHOTO = "PERSON_PHOTO";

    public static final String COLUMN_PERSONS_LASTNAME = "LASTNAME";
    public static final String COLUMN_PERSONS_FIRSTNAME = "FIRSTNAME";
    public static final String COLUMN_PERSONS_MIDNAME = "MIDNAME";
    public static final String COLUMN_PERSONS_DIVISION = "DIVISION";
    public static final String COLUMN_PERSONS_RADIO_LABEL = "RADIO_LABEL";
    public static final String COLUMN_PERSONS_SEX = "SEX";
    public static final String COLUMN_PERSONS_PHOTO_PREVIEW = "PHOTO_PREVIEW";
    public static final String COLUMN_PERSONS_PHOTO_ORIGINAL = "PHOTO_ORIGINAL";

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

    public static Connection SQLconnect;

    private ServerConnectionItem mServerConnectionItem;
    private SQL_Connection_interface mConnectionInterface;

    public SQL_Connection (ServerConnectionItem serverConnectionItem, SQL_Connection_interface sql_connection_interface){
        this.mServerConnectionItem = serverConnectionItem;
        this.mConnectionInterface = sql_connection_interface;
        System.out.println("sql connection ***************************************");
    }

    @Override
    protected Exception doInBackground(Void... params) {
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "KeyRegistratorBase";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(classs);
            String ConnURL = "jdbc:jtds:sqlserver://" + mServerConnectionItem.getServerName() + ";"
                    + "database=" + db +";user=" + mServerConnectionItem.getUserName() + ";password="
                    + mServerConnectionItem.getUserPassword() + ";";

            DriverManager.setLoginTimeout(5);

            Properties properties = new Properties();
            properties.put("connectTimeout", "60000");

            if (SQLconnect == null){
                SQLconnect = DriverManager.getConnection(ConnURL, properties);
            }

            return null;

        }catch (Exception e) {
            //e.printStackTrace();
            return e;
        }

    }

    @Override
    protected void onPostExecute(Exception e) {
        System.out.println("sql connection -------------------------------------");
        if (e != null){
            Settings.setServerStatus(false);
            if (mConnectionInterface!=null) mConnectionInterface.onServerConnectException(e);
            if (mConnectionInterface!=null) mConnectionInterface.onServerDisconnected();
        } else {
            Settings.setServerStatus(true);
            if (mConnectionInterface!=null) mConnectionInterface.onServerConnected();
        }
    }

    public interface SQL_Connection_interface{
        void onServerConnected();
        void onServerDisconnected();
        void onServerConnectException(Exception e);
    }
}
