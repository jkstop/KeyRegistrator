package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.os.StrictMode;

import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private static Thread connectThread;

    private static String mServerName;
    private static Callback mCallback;
    private static SQL_Connection mConnectTask;


    private static final String NET_SOURCEFORGE_JTDS_JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    private static final String DB = "KeyRegistratorBase";

    public SQL_Connection (String serverName, Callback callback){
        mServerName = serverName;
        mCallback = callback;
    }

    public static Connection getConnection(String serverName, Callback callback){
        mServerName = serverName;
        mCallback = callback;

        System.out.println("SQL CONNECT " + SQLconnect);
        System.out.println("THREAD " + connectThread);

        if (connectThread == null){
            connectThread = new Thread(null,getConnect,"BackGroundConnect");
            System.out.println("THREAD CREATED " + connectThread);
            connectThread.start();
        } else {

            connectThread.interrupt();

            System.out.println("THREAT INTERRUPT ");
        }
        System.out.println("THREAD STATE " + connectThread.getState());

        if (serverName !=null){
            //connectThread.start();
            //mConnectTask = new SQL_Connection(serverName, callback);
            //mConnectTask.execute();
            //connect(serverName, callback);
        } else {
            if (SQLconnect!=null){
                return SQLconnect;
            } else {
                mServerName = Settings.getServerName();
               // connectThread.start();
                //if (mConnectTask == null){
                //    mConnectTask = new SQL_Connection(Settings.getServerName(), callback);
                //    mConnectTask.execute();
               // }
                //connect(Settings.getServerName(), callback);
            }
        }
        return null;
    }

    private static Runnable getConnect = new Runnable() {
        @Override
        public void run() {
            getConnectionFromUrl(mServerName, mCallback);
        }
    };

   /* private static void connect(String serverName, Callback callback){
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Class.forName(NET_SOURCEFORGE_JTDS_JDBC_DRIVER);
            final String ConnURL = "jdbc:jtds:sqlserver://" + serverName + ";"
                    + "database=" + DB +";user=shsupport;password=podderzhka;";

            getConnectionFromUrl(ConnURL, callback).start();

        } catch (Exception e){
            e.printStackTrace();
        }
    }*/

    private static void getConnectionFromUrl(final String serverName, final Callback callback){

        System.out.println("START GET CONNECT SN " + serverName + " CB " + callback);


        for (int i=0;i<100;i++){
            while (!connectThread.isInterrupted()){
                System.out.println(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(NET_SOURCEFORGE_JTDS_JDBC_DRIVER);
            final String ConnURL = "jdbc:jtds:sqlserver://" + serverName + ";"
                    + "database=" + DB +";user=shsupport;password=podderzhka;";
            System.out.println("start get connection from URL");
            SQLconnect = DriverManager.getConnection(ConnURL);
            //connectThread = null;
            if (callback!=null) callback.onServerConnected(SQLconnect);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback!=null) callback.onServerConnectException(e);
        }*/
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
            Class.forName(NET_SOURCEFORGE_JTDS_JDBC_DRIVER);
            String ConnURL = "jdbc:jtds:sqlserver://" + mServerName + ";"
                    + "database=" + DB +";user=shsupport;password=podderzhka;";
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
        mConnectTask = null;
        if (e != null){
            Settings.setServerStatus(false);
            System.out.println("SERVER DISCONNECTED");
            if (mCallback!=null) mCallback.onServerConnectException(e);
            //if (mConnectionInterface!=null) mConnectionInterface.onServerConnectException(e);
            //if (mConnectionInterface!=null) mConnectionInterface.onServerDisconnected();
        } else {
            Settings.setServerStatus(true);
            if (mCallback!=null) mCallback.onServerConnected(SQLconnect);
            //if (mConnectionInterface!=null) mConnectionInterface.onServerConnected();
            System.out.println("SERVER CONNECTED");
        }
    }

    public interface Callback{
        void onServerConnected(Connection connection);
        void onServerConnectException(Exception e);
    }
}
