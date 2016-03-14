package com.example.ivsmirnov.keyregistrator.databases;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ivsmirnov.keyregistrator.others.App;

/**
 * Created by ivsmirnov on 11.03.2016.
 */
public class DB {

    public static final int DB_FAVORITE = 0;
    public static final int DB_JOURNAL = 1;
    public static final int DB_ROOM = 2;
    public static final int DB_ACCOUNT = 3;

    private static DataBaseFavoriteRegist mDataBaseFavoriteOpenHelper;
    private static DataBaseJournalRegist mDataBaseJournalOpenHelper;
    private static DataBaseRoomsRegist mDataBaseRoomsOpenHelper;
    private static DataBaseAccountRegist mDataBaseAccountOpenHelper;
    private static SQLiteDatabase mDataBaseFavorite, mDataBaseJournal, mDataBaseRooms, mDataBaseAccount;
    private static Cursor mCursor;

    public DB(){

        mDataBaseFavorite = getDataBase(DB_FAVORITE);
        mDataBaseJournal = getDataBase(DB_JOURNAL);
        mDataBaseRooms = getDataBase(DB_ROOM);
        mDataBaseAccount = getDataBase(DB_ACCOUNT);
    }

    public static SQLiteDatabase getDataBase(int db){
        switch (db){
            case DB_FAVORITE:
                if (mDataBaseFavorite == null){
                    mDataBaseFavoriteOpenHelper = new DataBaseFavoriteRegist(App.getAppContext());
                    mDataBaseFavorite = mDataBaseFavoriteOpenHelper.getWritableDatabase();
                }
                return mDataBaseFavorite;

            case DB_JOURNAL:
                if (mDataBaseJournal == null){
                    mDataBaseJournalOpenHelper = new DataBaseJournalRegist(App.getAppContext());
                    mDataBaseJournal = mDataBaseJournalOpenHelper.getWritableDatabase();
                }
                return mDataBaseJournal;

            case DB_ROOM:
                if (mDataBaseRooms == null){
                    mDataBaseRoomsOpenHelper = new DataBaseRoomsRegist(App.getAppContext());
                    mDataBaseRooms = mDataBaseRoomsOpenHelper.getWritableDatabase();
                }
                return mDataBaseRooms;

            case DB_ACCOUNT:
                if (mDataBaseAccount == null){
                    mDataBaseAccountOpenHelper = new DataBaseAccountRegist(App.getAppContext());
                    mDataBaseAccount = mDataBaseAccountOpenHelper.getWritableDatabase();
                }
                return mDataBaseAccount;

            default:
                    return null;
        }
    }

    public static Cursor getCursor(int db, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy, String limit){
        mCursor = getDataBase(db).query(table, columns, selection, selectionArgs, groupBy, null, orderBy, limit);
        return mCursor;
    }


    public static void closeDB(){

        if (mDataBaseFavoriteOpenHelper!=null) mDataBaseFavoriteOpenHelper.close();
        if (mDataBaseFavorite!=null) mDataBaseFavorite.close();
        if (mDataBaseJournalOpenHelper!=null) mDataBaseJournalOpenHelper.close();
        if (mDataBaseJournal!=null) mDataBaseJournal.close();
        if (mDataBaseRoomsOpenHelper!=null) mDataBaseRoomsOpenHelper.close();
        if (mDataBaseRooms!=null) mDataBaseRooms.close();
        if (mDataBaseAccountOpenHelper!=null) mDataBaseAccountOpenHelper.close();
        if (mDataBaseAccount!=null) mDataBaseAccount.close();
        if (mCursor !=null) mCursor.close();
    }
}
