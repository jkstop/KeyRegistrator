package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.StrictMode;

import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by ivsmirnov on 03.06.2016.
 */
public class Dummy extends Thread {

    private static final String NET_SOURCEFORGE_JTDS_JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    private static final String DB = "KeyRegistratorBase";


    private volatile Connection conn = null;
    @Override
    public void run() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(NET_SOURCEFORGE_JTDS_JDBC_DRIVER);
            final String ConnURL = "jdbc:jtds:sqlserver://" + Settings.getServerName() + ";"
                    + "database=" + DB +";user=shsupport;password=podderzhka;";
            System.out.println("start get connection from URL");
            this.conn = DriverManager.getConnection(ConnURL) ;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    static public Connection getConnection() {
        Dummy d = new Dummy() ;
        d.start() ;
        try {
            Thread.sleep(2000) ;
            d.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return d.conn ;
    }
}