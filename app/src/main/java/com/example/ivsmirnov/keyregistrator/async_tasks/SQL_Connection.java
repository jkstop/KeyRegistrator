package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Created by ivsmirnov on 26.02.2016.
 */
public class SQL_Connection extends AsyncTask<Void,Void,Connection> {

    private Context mContext;
    private ServerConnectionItem mServerConnectionItem;

    public SQL_Connection (Context context, ServerConnectionItem serverConnectionItem){
        this.mContext = context;
        this.mServerConnectionItem = serverConnectionItem;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Connection doInBackground(Void... params) {
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

            if (SQL_Connector.SQL_connection == null){
                SQL_Connector.SQL_connection = DriverManager.getConnection(ConnURL);
            }

            return SQL_Connector.SQL_connection;

        }catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    protected void onPostExecute(Connection connection) {
        if (connection == null){
            new Settings(mContext).setServerStatus(false);
        } else {
            new Settings(mContext).setServerStatus(true);
        }
        Log.d("connection", String.valueOf(connection));
    }
}
