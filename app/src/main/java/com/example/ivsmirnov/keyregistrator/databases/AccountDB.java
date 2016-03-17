package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.ivsmirnov.keyregistrator.items.AccountItem;

/**
 * Created by ivsmirnov on 28.01.2016.
 */
public class AccountDB {


    public static void writeAccount(AccountItem accountItem){
        String checkPreviousAccount = null;
        try {
            checkPreviousAccount = DbShare.getDataBase(DbShare.DB_ACCOUNT).compileStatement("SELECT * FROM " + AccountDBinit.TABLE_ACCOUNTS
                    + " WHERE " + AccountDBinit.COLUMN_ACCOUNT_ID + " = '" + accountItem.getAccountID() + "'").simpleQueryForString();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (checkPreviousAccount!=null){
            DbShare.getDataBase(DbShare.DB_ACCOUNT).delete(AccountDBinit.TABLE_ACCOUNTS,
                    AccountDBinit._ID + "=" + checkPreviousAccount,
                    null);
        }

        ContentValues cv = new ContentValues();
        cv.put(AccountDBinit.COLUMN_ACCOUNT_ID,accountItem.getAccountID());
        cv.put(AccountDBinit.COLUMN_ACCOUNT_NAME,accountItem.getEmail());
        cv.put(AccountDBinit.COLUMN_ACCOUNT_PERSON_LASTNAME,accountItem.getLastname());
        cv.put(AccountDBinit.COLUMN_ACCOUNT_PERSON_FIRSTNAME,accountItem.getFirstname());
        cv.put(AccountDBinit.COLUMN_ACCOUNT_PERSON_PHOTO,accountItem.getPhoto());
        DbShare.getDataBase(DbShare.DB_ACCOUNT).insert(AccountDBinit.TABLE_ACCOUNTS,null,cv);
    }

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();
    }

    public static AccountItem getAccount(String accountId){
        Cursor cursor = null;
        AccountItem accountItem = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_ACCOUNT,
                    AccountDBinit.TABLE_ACCOUNTS,
                    null,
                    AccountDBinit.COLUMN_ACCOUNT_ID + " =?",
                    new String[]{accountId},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                accountItem = new AccountItem().setLastname(cursor.getString(cursor.getColumnIndex(AccountDBinit.COLUMN_ACCOUNT_PERSON_LASTNAME)))
                        .setFirstname(cursor.getString(cursor.getColumnIndex(AccountDBinit.COLUMN_ACCOUNT_PERSON_FIRSTNAME)))
                        .setEmail(cursor.getString(cursor.getColumnIndex(AccountDBinit.COLUMN_ACCOUNT_NAME)))
                        .setPhoto(cursor.getString(cursor.getColumnIndex(AccountDBinit.COLUMN_ACCOUNT_PERSON_PHOTO)))
                        .setAccountID(cursor.getString(cursor.getColumnIndex(AccountDBinit.COLUMN_ACCOUNT_ID)));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return accountItem;
    }
}
