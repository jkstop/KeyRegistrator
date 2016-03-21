package com.example.ivsmirnov.keyregistrator.others;

import android.app.Application;
import android.content.Context;

/**
 * APP
 */
public class App extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }
}
