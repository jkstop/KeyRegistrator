package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 09.11.2015.
 */
public class DataBaseRooms {

    private Context mContext;
    private DataBaseRoomsRegist dataBaseRoomsRegist;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;

    public DataBaseRooms(Context context){
        this.mContext = context;
        dataBaseRoomsRegist = new DataBaseRoomsRegist(mContext);
        sqLiteDatabase = dataBaseRoomsRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseRoomsRegist.TABLE_ROOMS, null, null, null, null, null, null);
    }

    public void writeInRoomsDB (RoomItem roomItem) {
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_ROOM, roomItem.Auditroom);
        cv.put(DataBaseRoomsRegist.COLUMN_STATUS, roomItem.Status);
        cv.put(DataBaseRoomsRegist.COLUMN_ACCESS,roomItem.Access);
        cv.put(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE, roomItem.PositionInBase);
        cv.put(DataBaseRoomsRegist.COLUMN_LAST_VISITER, roomItem.LastVisiter);
        cv.put(DataBaseRoomsRegist.COLUMN_TAG,roomItem.Tag);
        cv.put(DataBaseRoomsRegist.COLUMN_PHOTO_PATH, roomItem.Photo);
        sqLiteDatabase.insert(DataBaseRoomsRegist.TABLE_ROOMS, null, cv);
    }

    public void updateRoom(RoomItem roomItem){

        String roomPositionInBase = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseRoomsRegist.TABLE_ROOMS + " WHERE " + DataBaseRoomsRegist.COLUMN_ROOM + " = " + roomItem.Auditroom).simpleQueryForString();
        ContentValues cv = new ContentValues();
        cv.put(DataBaseRoomsRegist.COLUMN_STATUS,roomItem.Status);
        cv.put(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE,roomItem.PositionInBase);
        cv.put(DataBaseRoomsRegist.COLUMN_LAST_VISITER,roomItem.LastVisiter);
        cv.put(DataBaseRoomsRegist.COLUMN_ACCESS,roomItem.Access);
        cv.put(DataBaseRoomsRegist.COLUMN_TAG,roomItem.Tag);
        cv.put(DataBaseRoomsRegist.COLUMN_PHOTO_PATH, roomItem.Photo);
        sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS,cv,DataBaseRoomsRegist._ID + "=" + roomPositionInBase, null);
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

    public ArrayList<RoomItem> readRoomsDB(){
        ArrayList<RoomItem> roomItems = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            roomItems.add(new RoomItem(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM)),
                    cursor.getInt(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_STATUS)),
                    cursor.getInt(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ACCESS)),
                    cursor.getLong(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE)),
                    cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_LAST_VISITER)),
                    cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_TAG)),
                    cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_PHOTO_PATH))));
        }
        return roomItems;
    }

    public void backupRoomsToFile(){
        File file = null;
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(mPath + "/Rooms.csv");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            String room = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM));
            String status = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_STATUS));
            String cardOrHand = cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ACCESS));
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
