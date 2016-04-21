package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * ДБ преподавателей
 */
public class FavoriteDBinit extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Favorites.db";
    private static final int version = 13;

    public static final String TABLE_PERSONS = "Пользователи";
    public static final String COLUMN_USER_ID_FAVORITE = "ID_Пользователя";
    public static final String COLUMN_LASTNAME_FAVORITE = "Фамилия";
    public static final String COLUMN_FIRSTNAME_FAVORITE = "Имя";
    public static final String COLUMN_MIDNAME_FAVORITE = "Отчество";
    public static final String COLUMN_DIVISION_FAVORITE = "Подразделение";
    public static final String COLUMN_TAG_FAVORITE = "Радиометка";
    public static final String COLUMN_SEX_FAVORITE = "Пол";
    public static final String COLUMN_PHOTO_PATH_FAVORITE = "Ссылка";
    public static final String COLUMN_ACCESS_TYPE = "Доступ";
    public static final String SQL_CREATE_TEACHERS_BASE = "create table " + TABLE_PERSONS + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_USER_ID_FAVORITE + " text, "
            + COLUMN_LASTNAME_FAVORITE + " text, "
            + COLUMN_FIRSTNAME_FAVORITE + " text, "
            + COLUMN_MIDNAME_FAVORITE + " text, "
            + COLUMN_DIVISION_FAVORITE + " text, "
            + COLUMN_TAG_FAVORITE + " text, "
            + COLUMN_SEX_FAVORITE + " text, "
            + COLUMN_ACCESS_TYPE + " integer, "
            + COLUMN_PHOTO_PATH_FAVORITE + " text);";

    public static final String SQL_DELETE_TEACHERS_BASE = "DROP TABLE IF EXISTS "
            + TABLE_PERSONS;

    public static  final String SQL_UPGRADE_TEACHERS_BASE = "ALTER TABLE " + TABLE_PERSONS + " ADD COLUMN " + COLUMN_ACCESS_TYPE + " INTEGER";

    public FavoriteDBinit(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TEACHERS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        //db.execSQL(SQL_UPGRADE_TEACHERS_BASE);
        db.execSQL(SQL_DELETE_TEACHERS_BASE);
        onCreate(db);
        Log.d("DataBase version update", "from " + String.valueOf(oldVersion) + " to " + String.valueOf(newVersion));

    }
}
