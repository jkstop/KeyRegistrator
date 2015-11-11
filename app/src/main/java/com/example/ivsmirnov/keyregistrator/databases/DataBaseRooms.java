package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 09.11.2015.
 */
public class DataBaseRooms {

    private Context mContext;
    private DataBaseRoomsRegist dataBaseRoomsRegist;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public DataBaseRooms(Context context){
        this.mContext = context;
        dataBaseRoomsRegist = new DataBaseRoomsRegist(mContext);
        sqLiteDatabase = dataBaseRoomsRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseRoomsRegist.TABLE_ROOMS, null, null, null, null, null, null);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
    }

    public void writeInRoomsDB (String room, String status,String cardOrHand, String lastVisiter, String tag,  String photoPath) {
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_ROOM, room);
        cv.put(DataBaseRoomsRegist.COLUMN_STATUS, status);
        cv.put(DataBaseRoomsRegist.COLUMN_CARD_OR_HANDLE,cardOrHand);
        cv.put(DataBaseRoomsRegist.COLUMN_LAST_VISITER, lastVisiter);
        cv.put(DataBaseRoomsRegist.COLUMN_TAG,tag);
        cv.put(DataBaseRoomsRegist.COLUMN_PHOTO_PATH, photoPath);
        long id = sqLiteDatabase.insert(DataBaseRoomsRegist.TABLE_ROOMS, null, cv);

        mSharedPreferencesEditor.putInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + room, (int) id);
        mSharedPreferencesEditor.apply();
    }

    public void updateStatusRooms(int id, String status){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_STATUS, status);
        sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS, cv, DataBaseRoomsRegist._ID + "=" + id, null);
    }

    public void updateCardOrHandle(int id, String cardOrHandle){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_CARD_OR_HANDLE, cardOrHandle);
        sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS, cv, DataBaseRoomsRegist._ID + "=" + id, null);
    }

    public void updateTagRoom(int id, String tag){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_TAG, tag);
        sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS, cv, DataBaseRoomsRegist._ID + "=" + id, null);
    }

    public void updateLastVisitersRoom(int id,String name){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_LAST_VISITER, name);
        sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS, cv, DataBaseRoomsRegist._ID + "=" + id, null);
    }

    public void updatePhotoPath(int id, String path){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_PHOTO_PATH, path);
        sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS, cv, DataBaseRoomsRegist._ID + "=" + id, null);
    }

    public void deleteFromRoomsDB(String aud){
        cursor = sqLiteDatabase.query(DataBaseRoomsRegist.TABLE_ROOMS,new String[]{DataBaseRoomsRegist._ID,DataBaseRoomsRegist.COLUMN_ROOM},
                null,null,null,null,null);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (aud.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM)))){
                sqLiteDatabase.delete(DataBaseRoomsRegist.TABLE_ROOMS,
                        DataBaseRoomsRegist._ID + "=" + cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist._ID)),
                        null);
            }
        }
    }

    public ArrayList<SparseArray<String>> readRoomsDB(){
        ArrayList<SparseArray<String>> items = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            SparseArray <String> card = new SparseArray<>();
            card.put(0,cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM)));
            card.put(1,cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_STATUS)));
            card.put(2,cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_CARD_OR_HANDLE)));
            card.put(3,cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_LAST_VISITER)));
            card.put(4,cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_TAG)));
            card.put(5,cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_PHOTO_PATH)));
            items.add(card);
        }
        return items;
    }

    public void backupRoomsToFile(){
        File file = null;
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(mPath + "/Rooms.txt");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            String room = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM));
            String status = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_STATUS));
            String cardOrHand = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_CARD_OR_HANDLE));
            String lastVisiter = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_LAST_VISITER));
            String tag = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_TAG));
            String photoPath = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_PHOTO_PATH));
            String stroke = room+";"+status+";"+cardOrHand+";"+lastVisiter+";"+tag+";"+photoPath;
            itemList.add(stroke);
        }
        try{
            if (file!=null){
                fileOutputStream = new FileOutputStream(file);
                for (int i=0;i<itemList.size();i++){
                    fileOutputStream.write(itemList.get(i).getBytes());
                    fileOutputStream.write("\n".getBytes());
                }
                fileOutputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        itemList.clear();
    }

    public void clearRoomsDB(){
        sqLiteDatabase.delete(DataBaseRoomsRegist.TABLE_ROOMS,null,null);
    }

    public void closeDB(){
        dataBaseRoomsRegist.close();
        sqLiteDatabase.close();
        cursor.close();
    }
}
