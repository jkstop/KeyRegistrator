package com.example.ivsmirnov.keyregistrator.databases;

import android.app.VoiceInteractor;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.custom_views.AccountItem;

import java.sql.ResultSet;

/**
 * Created by ivsmirnov on 28.01.2016.
 */
public class DataBaseAccount {

    private DataBaseAccountRegist dataBaseAccountRegist;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;

    private Context mContext;

    public DataBaseAccount(Context context){
        this.mContext = context;
        dataBaseAccountRegist = new DataBaseAccountRegist(mContext);
        sqLiteDatabase = dataBaseAccountRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseAccountRegist.TABLE_ACCOUNTS,null,null,null,null,null,null);
    }

    public void writeAccount(AccountItem accountItem){
        String checkPreviousAccount = null;
        try {
            checkPreviousAccount = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseAccountRegist.TABLE_ACCOUNTS
                    + " WHERE " + DataBaseAccountRegist.COLUMN_ACCOUNT_ID + " = '" + accountItem.AccountID + "'").simpleQueryForString();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (checkPreviousAccount!=null){
            sqLiteDatabase.delete(DataBaseAccountRegist.TABLE_ACCOUNTS,
                    DataBaseAccountRegist._ID + "=" + checkPreviousAccount,
                    null);
        }

        ContentValues cv = new ContentValues();
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_ID,accountItem.AccountID);
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME,accountItem.Email);
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME,accountItem.Lastname);
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME,accountItem.Firstname);
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO,accountItem.Photo);
        sqLiteDatabase.insert(DataBaseAccountRegist.TABLE_ACCOUNTS,null,cv);
    }

    public AccountItem getAccount(String accountId){
        AccountItem accountItem = null;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (accountId.equals(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID)))){
                accountItem = new AccountItem(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME)),
                        cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME)),
                        cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME)),
                        cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID)));
            }
        }
        return accountItem;
    }

    public void closeDB(){
        sqLiteDatabase.close();
        dataBaseAccountRegist.close();
        cursor.close();
    }
}
