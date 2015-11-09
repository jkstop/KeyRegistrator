package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ivsmirnov on 09.11.2015.
 */
public class DataBaseRoomsRegist extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Rooms.db";
    private static final int version = 3;

    public static final String TABLE_ROOMS = "Аудитории";
    public static final String COLUMN_ROOM = "Помещение";
    public static final String COLUMN_STATUS = "Статус";
    public static final String COLUMN_CARD_OR_HANDLE = "Картой_или_вручную";
    public static final String COLUMN_LAST_VISITER = "Последний";
    public static final String COLUMN_TAG = "Тэг";
    public static final String COLUMN_PHOTO_PATH = "Фото";
    public static final String CREATE_ROOMS_BASE = "CREATE TABLE " + TABLE_ROOMS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ROOM + " text not null, "
            + COLUMN_STATUS + " text not null, "
            + COLUMN_CARD_OR_HANDLE + " text not null, "
            + COLUMN_LAST_VISITER + " text not null, "
            + COLUMN_TAG + " text not null, "
            + COLUMN_PHOTO_PATH + " text not null);";

    public static final String DELETE_ROOMS_BASE = "DROP TABLE IF EXISTS " + TABLE_ROOMS;


    public DataBaseRoomsRegist(Context context) {
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
