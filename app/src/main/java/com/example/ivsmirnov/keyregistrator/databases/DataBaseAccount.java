package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ivsmirnov.keyregistrator.items.AccountItem;

import java.util.ArrayList;

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
                    + " WHERE " + DataBaseAccountRegist.COLUMN_ACCOUNT_ID + " = '" + accountItem.getAccountID() + "'").simpleQueryForString();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (checkPreviousAccount!=null){
            sqLiteDatabase.delete(DataBaseAccountRegist.TABLE_ACCOUNTS,
                    DataBaseAccountRegist._ID + "=" + checkPreviousAccount,
                    null);
        }

        ContentValues cv = new ContentValues();
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_ID,accountItem.getAccountID());
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME,accountItem.getEmail());
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME,accountItem.getLastname());
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME,accountItem.getFirstname());
        cv.put(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO,accountItem.getPhoto());
        sqLiteDatabase.insert(DataBaseAccountRegist.TABLE_ACCOUNTS,null,cv);
    }

    public AccountItem getAccount(String accountId){
        AccountItem accountItem = null;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (accountId.equals(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID)))){
                accountItem = new AccountItem().setLastname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME)))
                        .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME)))
                        .setEmail(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME)))
                        .setPhoto(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO)))
                        .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID)));
            }
        }
        return accountItem;
    }

    public ArrayList<AccountItem> getAllAccounts(){
        ArrayList<AccountItem> items = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            items.add(new AccountItem().setLastname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME)))
                    .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME)))
                    .setEmail(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME)))
                    .setPhoto(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO)))
                    .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID))));
        }
        return items;
    }

    public void closeDB(){
        sqLiteDatabase.close();
        dataBaseAccountRegist.close();
        cursor.close();
    }
}
