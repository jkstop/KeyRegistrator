package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * ДБ расписания
 */
public class SheduleDBinit extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Shedule.db";
    private static final int version = 10;

    public static final String TABLE_SHEDULE = "Расписание";
    public static final String COLUMN_TIME_START = "Начало";
    public static final String COLUMN_TIME_END = "Конец";
    public static final String COLUMN_GROUP_NAME = "Группа";
    public static final String COLUMN_TEACHER_NAME = "Преподаватель";
    public static final String COLUMN_AUDITROOM_NAME = "Аудитория";
    public static final String COLUMN_SUBJECT = "Предмет";
    public static final String COLUMN_PARITY = "Периодичность";

    private static final String SQL_CREATE_BASE_SHEDULE = "create table " + TABLE_SHEDULE + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_TIME_START + " text not null, " + COLUMN_TIME_END + " text not null, "
            + COLUMN_GROUP_NAME + " text not null, " + COLUMN_TEACHER_NAME + " text not null, "
            + COLUMN_AUDITROOM_NAME + " text not null, " + COLUMN_SUBJECT + " text not null, "
            + COLUMN_PARITY + " text not null);";
    private static final String SQL_DELETE_BASE_SHEDULE= "DROP TABLE IF EXISTS "
            + TABLE_SHEDULE;

    public SheduleDBinit(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BASE_SHEDULE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_BASE_SHEDULE);
        onCreate(db);

        Log.d("DataBase version update","from "+String.valueOf(oldVersion)+" to "+String.valueOf(newVersion));
    }
}
