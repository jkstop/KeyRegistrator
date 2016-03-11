package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by ivsmirnov on 11.03.2016.
 */
public class DBinit {

    private static Context mContext;

    private static DataBaseFavoriteRegist mDataBaseFavoriteOpenHelper;
    private static SQLiteDatabase mDataBaseFavorite;
    private static Cursor mCursorFavorite;

    public DBinit (Context context){
        mContext = context;

        mDataBaseFavoriteOpenHelper = new DataBaseFavoriteRegist(mContext);
        mDataBaseFavorite = mDataBaseFavoriteOpenHelper.getWritableDatabase();

        Log.d("DB_INIT","*********FAVORITE*********");
    }

    public static SQLiteDatabase getDataBaseFavorite(){
        if (isFavoriteDBinited()){
            Log.d("DB_RETURN","*********FAVORITE********");
            return mDataBaseFavorite;
        } else {
            Log.d("DB_RECREATE","*********FAVORITE********");
            mDataBaseFavoriteOpenHelper = new DataBaseFavoriteRegist(mContext);
            return mDataBaseFavoriteOpenHelper.getWritableDatabase();
        }
    }

    public static Cursor getCursorFavorite(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy, String limit){
        mCursorFavorite = getDataBaseFavorite().query(table, columns, selection, selectionArgs, groupBy, null, orderBy, limit);
        return mCursorFavorite;
    }

    public static boolean isFavoriteDBinited(){
        return mDataBaseFavorite != null;
    }

    public static void closeDB(){
        Log.d("DB_CLOSE","*********FAVORITE********");
        if (mDataBaseFavoriteOpenHelper!=null) mDataBaseFavoriteOpenHelper.close();
        if (mDataBaseFavorite!=null) mDataBaseFavorite.close();
        if (mCursorFavorite!=null) mCursorFavorite.close();
    }
}
