package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * ДБ аккаунтов
 */
public class AccountDBinit extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Accounts.db";
    private static final int version = 1;

    public static final String TABLE_ACCOUNTS = "Список";
    public static final String COLUMN_ACCOUNT_ID = "id_аккаунта";
    public static final String COLUMN_ACCOUNT_NAME = "Имя_аккаунта";
    public static final String COLUMN_ACCOUNT_PERSON_LASTNAME = "Фамилия";
    public static final String COLUMN_ACCOUNT_PERSON_FIRSTNAME = "Имя";
    public static final String COLUMN_ACCOUNT_PERSON_PHOTO = "Фото";

    private static final String SQL_CREATE_TEACHERS_BASE = "create table " + TABLE_ACCOUNTS + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_ACCOUNT_ID + " text, "
            + COLUMN_ACCOUNT_NAME + " text, "
            + COLUMN_ACCOUNT_PERSON_LASTNAME + " text, "
            + COLUMN_ACCOUNT_PERSON_FIRSTNAME + " text, "
            + COLUMN_ACCOUNT_PERSON_PHOTO + " text);";

    private static final String SQL_DELETE_ACCOUNTS_BASE = "DROP TABLE IF EXISTS "
            + TABLE_ACCOUNTS;

    public AccountDBinit(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TEACHERS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ACCOUNTS_BASE);
        onCreate(db);
        Log.d("DataBase version update", "from " + String.valueOf(oldVersion) + " to " + String.valueOf(newVersion));
    }
}
