package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class DataBaseFavoriteRegist extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Favorites.db";
    private static final int version = 3;

    public static final String TABLE_TEACHER = "Список";
    public static final String COLUMN_SURNAME_FAVORITE = "Фамилия";
    public static final String COLUMN_NAME_FAVORITE = "Имя";
    public static final String COLUMN_LASTNAME_FAVORITE = "Отчество";
    public static final String COLUMN_KAF_FAVORITE = "Кафедра";
    public static final String COLUMN_TAG_FAVORITE = "Радиометка";
    public static final String COLUMN_GENDER_FAVORITE = "Пол";
    public static final String COLUMN_PHOTO_FAVORITE = "Фото";
    public static final String COLUMN_PHOTO_ORIGINAL_FAVORITE = "Оригинал";
    public static final String SQL_CREATE_TEACHERS_BASE = "create table " + TABLE_TEACHER + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_SURNAME_FAVORITE + " text not null, " + COLUMN_NAME_FAVORITE + " text not null, " + COLUMN_LASTNAME_FAVORITE + " text not null, " +
            COLUMN_KAF_FAVORITE + " text not null, " + COLUMN_TAG_FAVORITE + " text not null, " + COLUMN_GENDER_FAVORITE + " text not null, " +
            COLUMN_PHOTO_FAVORITE + " text not null, " + COLUMN_PHOTO_ORIGINAL_FAVORITE + " text not null);";
    public static final String SQL_DELETE_TEACHERS_BASE = "DROP TABLE IF EXISTS "
            + TABLE_TEACHER;

    public DataBaseFavoriteRegist(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TEACHERS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TEACHERS_BASE);
        onCreate(db);
        Log.d("DataBase version update", "from " + String.valueOf(oldVersion) + " to " + String.valueOf(newVersion));

    }
}
