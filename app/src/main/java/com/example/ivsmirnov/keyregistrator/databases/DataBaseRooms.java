package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Values;

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

    public DataBaseRooms(Context context){
        this.mContext = context;
        dataBaseRoomsRegist = new DataBaseRoomsRegist(mContext);
        sqLiteDatabase = dataBaseRoomsRegist.getWritableDatabase();
        Log.d("ROOMS_DB","-------------CREATE---------------");
    }

    public void writeInRoomsDB (RoomItem roomItem) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DataBaseRoomsRegist.COLUMN_ROOM, roomItem.getAuditroom());
            cv.put(DataBaseRoomsRegist.COLUMN_STATUS, roomItem.getStatus());
            cv.put(DataBaseRoomsRegist.COLUMN_ACCESS,roomItem.getAccessType());
            cv.put(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE, roomItem.getPositionInBase());
            cv.put(DataBaseRoomsRegist.COLUMN_LAST_VISITER, roomItem.getLastVisiter());
            cv.put(DataBaseRoomsRegist.COLUMN_TAG,roomItem.getTag());
            cv.put(DataBaseRoomsRegist.COLUMN_PHOTO_PATH, roomItem.getPhoto());
            sqLiteDatabase.insert(DataBaseRoomsRegist.TABLE_ROOMS, null, cv);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public int updateRoom(RoomItem roomItem){
        int updatedRooms = 0;
        try {
            String roomPosition;
            if (!roomItem.getAuditroom().equals(Values.EMPTY)){
                roomPosition = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseRoomsRegist.TABLE_ROOMS +
                        " WHERE " + DataBaseRoomsRegist.COLUMN_ROOM + " = " + roomItem.getAuditroom()).simpleQueryForString();
            }else{
                roomPosition = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseRoomsRegist.TABLE_ROOMS +
                        " WHERE " + DataBaseRoomsRegist.COLUMN_TAG + " = " + roomItem.getTag()).simpleQueryForString();
            }

            if (roomPosition!=null){
                ContentValues cv = new ContentValues();
                cv.put(DataBaseRoomsRegist.COLUMN_STATUS,roomItem.getStatus());
                cv.put(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE,roomItem.getPositionInBase());
                cv.put(DataBaseRoomsRegist.COLUMN_LAST_VISITER,roomItem.getLastVisiter());
                cv.put(DataBaseRoomsRegist.COLUMN_ACCESS,roomItem.getAccessType());
                cv.put(DataBaseRoomsRegist.COLUMN_TAG,roomItem.getTag());
                cv.put(DataBaseRoomsRegist.COLUMN_PHOTO_PATH, roomItem.getPhoto());
                sqLiteDatabase.update(DataBaseRoomsRegist.TABLE_ROOMS,cv,DataBaseRoomsRegist._ID + "=" + roomPosition, null);
                updatedRooms++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return updatedRooms;
    }

    public RoomItem getRoomItemForCurrentUser(String tag){
        Cursor cursor = null;
        try {
            if (tag!=null){
                cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseRoomsRegist.TABLE_ROOMS
                                + " WHERE " + DataBaseRoomsRegist.COLUMN_TAG + " =?",
                        new String[]{tag});
                if (cursor.getCount() > 0){
                    cursor.moveToFirst();
                    return new RoomItem().setAuditroom(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM)))
                            .setStatus(cursor.getInt(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_STATUS)))
                            .setAccessType(cursor.getInt(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ACCESS)))
                            .setLastVisiter(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_LAST_VISITER)))
                            .setPositionInBase(cursor.getLong(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE)))
                            .setTag(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_TAG))); /* photo is null for speed*/
                }else{
                    return null;
                }
            }else{
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return new RoomItem();
        } finally {
            closeCursor(cursor);
        }
    }



    public void deleteFromRoomsDB(String aud){
        Cursor cursor = null;
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

    }

    public ArrayList<RoomItem> readRoomsDB(){
        Cursor cursor = null;
        try {
            ArrayList<RoomItem> roomItems = new ArrayList<>();
            cursor = sqLiteDatabase.query(DataBaseRoomsRegist.TABLE_ROOMS,null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                roomItems.add(new RoomItem().setAuditroom(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ROOM)))
                        .setStatus(cursor.getInt(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_STATUS)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_ACCESS)))
                        .setPositionInBase(cursor.getLong(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_POSITION_IN_BASE)))
                        .setLastVisiter(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_LAST_VISITER)))
                        .setTag(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_TAG)))
                        .setPhoto(cursor.getString(cursor.getColumnIndex(DataBaseRoomsRegist.COLUMN_PHOTO_PATH))));
            }
            return roomItems;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }

    }

    public void backupRoomsToFile(){
        Cursor cursor = null;
        try {
            File file = null;
            ArrayList <String> itemList = new ArrayList<>();
            FileOutputStream fileOutputStream;
            String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(mPath + "/Rooms.csv");
            cursor = sqLiteDatabase.query(DataBaseRoomsRegist.TABLE_ROOMS,null,null,null,null,null,null);
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
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

    }

    public void clearRoomsDB(){
        try {
            sqLiteDatabase.delete(DataBaseRoomsRegist.TABLE_ROOMS,null,null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void closeDB(){
        if (dataBaseRoomsRegist!=null){
            dataBaseRoomsRegist.close();
        }

        if (sqLiteDatabase!=null){
             sqLiteDatabase.close();
         }
    }

    private void closeCursor(Cursor cursor){
        if (cursor!=null){
            cursor.close();
        }
    }
}
