package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ivsmirnov on 03.11.2015.
 */
public class DataBaseStaffRegist extends SQLiteOpenHelper implements BaseColumns {

    public static final String name = "Staff.db";
    public static final int version = 3;

    public static final String TABLE_STAFF = "Сотрудники";
    public static final String COLUMN_NAME_DIVISION = "Подразделение";
    public static final String COLUMN_NAME_POSITION = "Должность";
    public static final String COLUMN_LASTNAME = "Фамилия";
    public static final String COLUMN_FIRSTNAME = "Имя";
    public static final String COLUMN_MIDNAME = "Отчество";
    public static final String COLUMN_SEX = "Пол";
    public static final String COLUMN_PHOTO = "Фото";
    public static final String COLUMN_RADIO_LABEL = "Радиометка";

    private static final String SQL_CREATE_BASE_STAFF = "create table " + TABLE_STAFF + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_NAME_DIVISION + " text not null, " + COLUMN_NAME_POSITION + " text not null, "
            + COLUMN_LASTNAME + " text not null, " + COLUMN_FIRSTNAME + " text not null, "
            + COLUMN_MIDNAME + " text not null, " + COLUMN_SEX + " text not null, "
            + COLUMN_PHOTO + " text not null, "+ COLUMN_RADIO_LABEL + " text not null);";

    private static final String SQL_DELETE_BASE_STAFF= "DROP TABLE IF EXISTS "
            + TABLE_STAFF;

    public DataBaseStaffRegist(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BASE_STAFF);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_BASE_STAFF);
        onCreate(db);

        Log.d("DataBase version update", "from " + String.valueOf(oldVersion) + " to " + String.valueOf(newVersion));
    }
}
