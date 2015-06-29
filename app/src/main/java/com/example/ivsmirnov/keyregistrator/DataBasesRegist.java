package com.example.ivsmirnov.keyregistrator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.provider.BaseColumns;
import android.util.Log;

public class DataBasesRegist extends SQLiteOpenHelper implements BaseColumns{

    private static final String name = "Key_registrator_database.db";
    private static final int version = 13;

    public static final String TABLE_JOURNAL = "Журнал";
    public static final String COLUMN_AUD = "Аудитория";
    public static final String COLUMN_NAME = "Фамилия";
    public static final String COLUMN_TIME = "Взял";
    public static final String COLUMN_TIME_PUT = "Сдал";
    private static final String SQL_CREATE_BASE_JOURNAL = "create table " + TABLE_JOURNAL + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_AUD + " integer, " + COLUMN_NAME + " text not null, " + COLUMN_TIME + " long, " + COLUMN_TIME_PUT + " long);";
    private static final String SQL_DELETE_BASE_JOURNAL = "DROP TABLE IF EXISTS "
            + TABLE_JOURNAL;

    public static final String TABLE_TEACHER = "Список";
    public static final String COLUMN_TEACHER = "ФИО";
    public static final String SQL_CREATE_TEACHERS_BASE = "create table " + TABLE_TEACHER + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_TEACHER + " text not null);";
    public static final String SQL_DELETE_TEACHERS_BASE = "DROP TABLE IF EXISTS "
            + TABLE_TEACHER;

    public static final String TABLE_ROOMS = "Аудитории";
    public static final String COLUMN_ROOM = "Помещение";
    public static final String COLUMN_STATUS = "Статус";
    public static final String COLUMN_LAST_VISITER = "Последний";
    public static final String CREATE_ROOMS_BASE = "CREATE TABLE " + TABLE_ROOMS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ROOM + " integer, " + COLUMN_STATUS + " integer, "+ COLUMN_LAST_VISITER + " text not null);";
    public static final String DELETE_ROOMS_BASE = "DROP TABLE IF EXISTS " + TABLE_ROOMS;

    public DataBasesRegist(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_BASE_JOURNAL);
        sqLiteDatabase.execSQL(SQL_CREATE_TEACHERS_BASE);
        sqLiteDatabase.execSQL(CREATE_ROOMS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_BASE_JOURNAL);
        sqLiteDatabase.execSQL(SQL_DELETE_TEACHERS_BASE);
        sqLiteDatabase.execSQL(DELETE_ROOMS_BASE);
        onCreate(sqLiteDatabase);

        Log.d("DataBase version update","from "+String.valueOf(oldVersion)+" to "+String.valueOf(newVersion));
    }


}
