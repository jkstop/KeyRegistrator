package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * ДБ помещений
 */
class RoomDBinit extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Rooms.db";
    private static final int version = 8;

    public static final String TABLE_ROOMS = "Аудитории";
    public static final String COLUMN_ROOM = "Помещение";
    public static final String COLUMN_STATUS = "Статус";
    public static final String COLUMN_ACCESS = "Доступ";
    public static final String COLUMN_TIME = "Время";
    public static final String COLUMN_LAST_VISITER = "Последний";
    public static final String COLUMN_TAG = "Тэг";
    private static final String COLUMN_PHOTO_PATH = "Фото";
    private static final String CREATE_ROOMS_BASE = "CREATE TABLE " + TABLE_ROOMS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ROOM + " text, "
            + COLUMN_STATUS + " integer, "
            + COLUMN_ACCESS + " integer, "
            + COLUMN_TIME + " long, "
            + COLUMN_LAST_VISITER + " text, "
            + COLUMN_TAG + " text, "
            + COLUMN_PHOTO_PATH + " text);";

    private static final String DELETE_ROOMS_BASE = "DROP TABLE IF EXISTS " + TABLE_ROOMS;


    public RoomDBinit(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ROOMS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_ROOMS_BASE);
        onCreate(db);
        Log.d("DataBase version update", "from " + String.valueOf(oldVersion) + " to " + String.valueOf(newVersion));
    }
}
