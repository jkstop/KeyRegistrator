package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ivsmirnov on 10.11.2015.
 */
public class DataBaseJournalRegist extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Journal.db";
    private static final int version = 1;

    public static final String TABLE_JOURNAL = "Журнал";
    public static final String COLUMN_AUD = "Аудитория";
    public static final String COLUMN_NAME = "Фамилия";
    public static final String COLUMN_TIME = "Взял";
    public static final String COLUMN_TIME_PUT = "Сдал";
    private static final String SQL_CREATE_BASE_JOURNAL = "create table " + TABLE_JOURNAL + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_AUD + " integer, " + COLUMN_NAME + " text not null, " + COLUMN_TIME + " long, " + COLUMN_TIME_PUT + " long);";
    private static final String SQL_DELETE_BASE_JOURNAL = "DROP TABLE IF EXISTS "
            + TABLE_JOURNAL;

    public DataBaseJournalRegist(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BASE_JOURNAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_BASE_JOURNAL);
        onCreate(db);

        Log.d("DataBase version update", "from " + String.valueOf(oldVersion) + " to " + String.valueOf(newVersion));
    }
}
