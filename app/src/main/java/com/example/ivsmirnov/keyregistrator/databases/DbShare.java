package com.example.ivsmirnov.keyregistrator.databases;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ivsmirnov.keyregistrator.others.App;

/**
 * init and share databases
 */
public class DbShare {

    public static final int DB_FAVORITE = 0;
    public static final int DB_JOURNAL = 1;
    public static final int DB_ROOM = 2;
    public static final int DB_ACCOUNT = 3;

    private static FavoriteDBinit mDataBaseFavoriteOpenHelper;
    private static JournalDBinit mDataBaseJournalOpenHelper;
    private static RoomDBinit mDataBaseRoomsOpenHelper;
    private static AccountDBinit mDataBaseAccountOpenHelper;
    private static SQLiteDatabase mDataBaseFavorite, mDataBaseJournal, mDataBaseRooms, mDataBaseAccount;
    private static Cursor mCursor;

    public DbShare(){

        mDataBaseFavorite = getDataBase(DB_FAVORITE);
        mDataBaseJournal = getDataBase(DB_JOURNAL);
        mDataBaseRooms = getDataBase(DB_ROOM);
        mDataBaseAccount = getDataBase(DB_ACCOUNT);
    }

    public static SQLiteDatabase getDataBase(int db){
        switch (db){
            case DB_FAVORITE:
                if (mDataBaseFavorite == null || !mDataBaseFavorite.isOpen()){
                    mDataBaseFavoriteOpenHelper = new FavoriteDBinit(App.getAppContext());
                    mDataBaseFavorite = mDataBaseFavoriteOpenHelper.getWritableDatabase();
                }
                return mDataBaseFavorite;

            case DB_JOURNAL:
                if (mDataBaseJournal == null || !mDataBaseJournal.isOpen()){
                    mDataBaseJournalOpenHelper = new JournalDBinit(App.getAppContext());
                    mDataBaseJournal = mDataBaseJournalOpenHelper.getWritableDatabase();
                }
                return mDataBaseJournal;

            case DB_ROOM:
                if (mDataBaseRooms == null || !mDataBaseRooms.isOpen()){
                    mDataBaseRoomsOpenHelper = new RoomDBinit(App.getAppContext());
                    mDataBaseRooms = mDataBaseRoomsOpenHelper.getWritableDatabase();
                }
                return mDataBaseRooms;

            case DB_ACCOUNT:
                if (mDataBaseAccount == null || !mDataBaseAccount.isOpen()){
                    mDataBaseAccountOpenHelper = new AccountDBinit(App.getAppContext());
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
