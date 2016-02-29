package com.example.ivsmirnov.keyregistrator.others;

import android.content.Context;
import android.os.StrictMode;

import com.example.ivsmirnov.keyregistrator.items.ServerConnectionItem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Created by ivsmirnov on 09.12.2015.
 */
public abstract class SQL_Connector implements Connection {

    public static Connection SQL_connection;

    /*public static Connection check_sql_connection(Context context, ServerConnectionItem serverConnectionItem){
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
            Properties properties = new Properties();
            properties.put("connectTimeout", "200");
            SQL_connection = DriverManager.getConnection(ConnURL,properties);
            new Settings(context).setServerStatus(true);
            return SQL_connection;
        }catch (Exception e) {
            e.printStackTrace();
            new Settings(context).setServerStatus(false);
            return null;
        }
    }*/
}
