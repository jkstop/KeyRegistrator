package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.items.AccountItem;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 28.01.2016.
 */
public class DataBaseAccount {

    private DataBaseAccountRegist dataBaseAccountRegist;
    private SQLiteDatabase sqLiteDatabase;

    private Context mContext;

    public DataBaseAccount(Context context){
        this.mContext = context;
        dataBaseAccountRegist = new DataBaseAccountRegist(mContext);
        sqLiteDatabase = dataBaseAccountRegist.getWritableDatabase();
        Log.d("ACCOUNT_DB","-------------CREATE---------------");
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

    private void closeCursor(Cursor cursor){
        if (cursor!=null){
            cursor.close();
        }
    }

    public AccountItem getAccount(String accountId){
        Cursor cursor = null;
        AccountItem accountItem = null;
        try {

            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseAccountRegist.TABLE_ACCOUNTS + " WHERE " + DataBaseAccountRegist.COLUMN_ACCOUNT_ID + " =?",new String[]{accountId});
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                accountItem = new AccountItem().setLastname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME)))
                        .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME)))
                        .setEmail(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME)))
                        .setPhoto(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO)))
                        .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID)));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return accountItem;
    }

    public ArrayList<AccountItem> getAllAccounts(){
        Cursor cursor = null;
        ArrayList<AccountItem> items = new ArrayList<>();
        try {
            cursor = sqLiteDatabase.query(DataBaseAccountRegist.TABLE_ACCOUNTS,null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(new AccountItem().setLastname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_LASTNAME)))
                        .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_FIRSTNAME)))
                        .setEmail(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_NAME)))
                        .setPhoto(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_PERSON_PHOTO)))
                        .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseAccountRegist.COLUMN_ACCOUNT_ID))));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return items;
    }

    public void closeDB(){
        sqLiteDatabase.close();
        dataBaseAccountRegist.close();
    }
}
