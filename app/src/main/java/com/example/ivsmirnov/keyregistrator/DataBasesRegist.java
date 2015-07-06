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
    private static final int version = 17;

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
    public static final String COLUMN_SURNAME_FAVORITE = "Фамилия";
    public static final String COLUMN_NAME_FAVORITE = "Имя";
    public static final String COLUMN_LASTNAME_FAVORITE = "Отчество";
    public static final String COLUMN_KAF_FAVORITE = "Кафедра";
    public static final String COLUMN_GENDER_FAVORITE = "Пол";
    public static final String SQL_CREATE_TEACHERS_BASE = "create table " + TABLE_TEACHER + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_SURNAME_FAVORITE + " text not null, " + COLUMN_NAME_FAVORITE + " text not null, " + COLUMN_LASTNAME_FAVORITE + " text not null, " +
            COLUMN_KAF_FAVORITE + " text not null, " + COLUMN_GENDER_FAVORITE + " text not null);";
    public static final String SQL_DELETE_TEACHERS_BASE = "DROP TABLE IF EXISTS "
            + TABLE_TEACHER;

    public static final String TABLE_ROOMS = "Аудитории";
    public static final String COLUMN_ROOM = "Помещение";
    public static final String COLUMN_STATUS = "Статус";
    public static final String COLUMN_LAST_VISITER = "Последний";
    public static final String CREATE_ROOMS_BASE = "CREATE TABLE " + TABLE_ROOMS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ROOM + " integer, " + COLUMN_STATUS + " integer, "+ COLUMN_LAST_VISITER + " text not null);";
    public static final String DELETE_ROOMS_BASE = "DROP TABLE IF EXISTS " + TABLE_ROOMS;

    public static final String TABLE_BASE = "База";
    public static final String COLUMN_KAF = "Кафедра";
    public static final String COLUMN_IMYA = "Имя";
    public static final String COLUMN_FAMILIA = "Фамилия";
    public static final String COLUMN_OTCHESTVO = "Отчество";
    public static final String CREATE_BASE_SQL = "CREATE TABLE " + TABLE_BASE + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_KAF + " text not null, " + COLUMN_IMYA + " text not null, "+ COLUMN_FAMILIA + " text not null, " + COLUMN_OTCHESTVO + " text not null);";
    public static final String DELETE_BASE_SQL = "DROP TABLE IF EXISTS " + TABLE_BASE;

    public DataBasesRegist(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_BASE_JOURNAL);
        sqLiteDatabase.execSQL(SQL_CREATE_TEACHERS_BASE);
        sqLiteDatabase.execSQL(CREATE_ROOMS_BASE);
        sqLiteDatabase.execSQL(CREATE_BASE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_BASE_JOURNAL);
        sqLiteDatabase.execSQL(SQL_DELETE_TEACHERS_BASE);
        sqLiteDatabase.execSQL(DELETE_ROOMS_BASE);
        sqLiteDatabase.execSQL(DELETE_BASE_SQL);
        onCreate(sqLiteDatabase);

        Log.d("DataBase version update","from "+String.valueOf(oldVersion)+" to "+String.valueOf(newVersion));
    }


}
