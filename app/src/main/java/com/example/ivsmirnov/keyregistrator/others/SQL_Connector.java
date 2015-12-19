package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by ivsmirnov on 09.12.2015.
 */
public abstract class SQL_Connector implements Connection {

    public static boolean check_sql_connection(Context context, String server, String user, String password){
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "KeyRegistratorBase";
        if (server==null){
            server = mSharedPreferences.getString(Values.SQL_SERVER,"");
        }
        if (user==null){
            user = mSharedPreferences.getString(Values.SQL_USER,"");
        }
        if (password==null){
            password = mSharedPreferences.getString(Values.SQL_PASSWORD,"");
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection conn = null;
        String ConnURL = null;
        boolean isConnected = false;

        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + server + ";"
                    + "database=" + db +";user=" + user + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
            isConnected = true;
            mSharedPreferencesEditor.putInt(Values.SQL_STATUS,Values.SQL_STATUS_CONNECT);
            mSharedPreferencesEditor.apply();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            isConnected = false;
            mSharedPreferencesEditor.putInt(Values.SQL_STATUS,Values.SQL_STATUS_DISCONNECT);
            mSharedPreferencesEditor.apply();
        }
        return isConnected;
    }
}
