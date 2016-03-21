package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * ДБ журнал
 */
public class JournalDBinit extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Journal.db";
    private static final int version = 4;

    public static final String TABLE_JOURNAL = "Журнал";
    public static final String COLUMN_USER_ID = "id_пользователя";
    public static final String COLUMN_AUD = "Аудитория";
    public static final String COLUMN_TIME_IN = "Взял";
    public static final String COLUMN_TIME_OUT = "Сдал";
    public static final String COLUMN_ACCESS_TYPE = "Доступ";
    public static final String COLUMN_PERSON_LASTNAME = "Фамилия";
    public static final String COLUMN_PERSON_FIRSTNAME = "Имя";
    public static final String COLUMN_PERSON_MIDNAME = "Отчество";
    public static final String COLUMN_PERSON_PHOTO = "Фото";

    private static final String SQL_CREATE_BASE_JOURNAL = "create table " + TABLE_JOURNAL + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_USER_ID + " text not null, "
            + COLUMN_AUD + " text not null, "
            + COLUMN_TIME_IN + " long, "
            + COLUMN_TIME_OUT + " long, "
            + COLUMN_ACCESS_TYPE + " integer, "
            + COLUMN_PERSON_LASTNAME + " text not null, "
            + COLUMN_PERSON_FIRSTNAME + " text not null, "
            + COLUMN_PERSON_MIDNAME + " text not null, "
            + COLUMN_PERSON_PHOTO + " text not null);";

    private static final String SQL_DELETE_BASE_JOURNAL = "DROP TABLE IF EXISTS "
            + TABLE_JOURNAL;

    public JournalDBinit(Context context) {
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
