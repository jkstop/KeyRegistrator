package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.custom_views.ServerConnectionItem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by ivsmirnov on 09.12.2015.
 */
public abstract class SQL_Connector implements Connection {

    public static boolean check_sql_connection(Context context, ServerConnectionItem serverConnectionItem){
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "KeyRegistratorBase";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(classs);
            String ConnURL = "jdbc:jtds:sqlserver://" + serverConnectionItem.getServerName() + ";"
                    + "database=" + db +";user=" + serverConnectionItem.getUserName() + ";password="
                    + serverConnectionItem.getUserPassword() + ";";
            Connection conn = DriverManager.getConnection(ConnURL);
            new Settings(context).setServerStatus(true);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            new Settings(context).setServerStatus(false);
            return false;
        }
    }
}
