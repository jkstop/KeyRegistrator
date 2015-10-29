package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class DataBaseShedule {

    public DataBasesSheduleRegist dataBasesSheduleRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;

    private Context mContext;

    public DataBaseShedule (Context context){
        this.mContext = context;

        dataBasesSheduleRegist = new DataBasesSheduleRegist(mContext);
        sqLiteDatabase = dataBasesSheduleRegist.getWritableDatabase();
    }
}
