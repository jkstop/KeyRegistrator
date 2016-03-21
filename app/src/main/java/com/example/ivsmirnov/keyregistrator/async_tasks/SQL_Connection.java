package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Соединение с сервером
 */
public class SQL_Connection extends AsyncTask<Void,Void,Exception> {

    public static Connection SQLconnect;

    private ServerConnectionItem mServerConnectionItem;
    private SQL_Connection_interface mConnectionInterface;

    public SQL_Connection (ServerConnectionItem serverConnectionItem, SQL_Connection_interface sql_connection_interface){
        this.mServerConnectionItem = serverConnectionItem;
        this.mConnectionInterface = sql_connection_interface;
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

            if (SQLconnect == null){
                SQLconnect = DriverManager.getConnection(ConnURL);
            }

            return null;

        }catch (Exception e) {
            //e.printStackTrace();
            return e;
        }

    }

    @Override
    protected void onPostExecute(Exception e) {
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
