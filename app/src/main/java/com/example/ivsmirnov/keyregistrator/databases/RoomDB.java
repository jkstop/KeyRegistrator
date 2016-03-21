package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;

import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ДБ помещений
 */
public class RoomDB {

    public static final int ROOM_IS_BUSY = 0;
    public static final int ROOM_IS_FREE = 1;

    public static void writeInRoomsDB (RoomItem roomItem) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(RoomDBinit.COLUMN_ROOM, roomItem.getAuditroom());
            cv.put(RoomDBinit.COLUMN_STATUS, roomItem.getStatus());
            cv.put(RoomDBinit.COLUMN_ACCESS,roomItem.getAccessType());
            cv.put(RoomDBinit.COLUMN_POSITION_IN_BASE, roomItem.getPositionInBase());
            cv.put(RoomDBinit.COLUMN_LAST_VISITER, roomItem.getLastVisiter());
            cv.put(RoomDBinit.COLUMN_TAG,roomItem.getTag());
            cv.put(RoomDBinit.COLUMN_PHOTO_PATH, roomItem.getPhoto());
            DbShare.getDataBase(DbShare.DB_ROOM).insert(RoomDBinit.TABLE_ROOMS, null, cv);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static int updateRoom(RoomItem roomItem){
        int updatedRooms = 0;
        try {
            String roomPosition;
            if (!roomItem.getAuditroom().equals(Values.EMPTY)){
                roomPosition = DbShare.getDataBase(DbShare.DB_ROOM).compileStatement("SELECT * FROM " + RoomDBinit.TABLE_ROOMS +
                        " WHERE " + RoomDBinit.COLUMN_ROOM + " = " + roomItem.getAuditroom()).simpleQueryForString();
            }else{
                roomPosition = DbShare.getDataBase(DbShare.DB_ROOM).compileStatement("SELECT * FROM " + RoomDBinit.TABLE_ROOMS +
                        " WHERE " + RoomDBinit.COLUMN_TAG + " = " + roomItem.getTag()).simpleQueryForString();
            }

            if (roomPosition!=null){
                ContentValues cv = new ContentValues();
                cv.put(RoomDBinit.COLUMN_STATUS,roomItem.getStatus());
                cv.put(RoomDBinit.COLUMN_POSITION_IN_BASE,roomItem.getPositionInBase());
                cv.put(RoomDBinit.COLUMN_LAST_VISITER,roomItem.getLastVisiter());
                cv.put(RoomDBinit.COLUMN_ACCESS,roomItem.getAccessType());
                cv.put(RoomDBinit.COLUMN_TAG,roomItem.getTag());
                //cv.put(RoomDBinit.COLUMN_PHOTO_PATH, roomItem.getPhoto());
                DbShare.getDataBase(DbShare.DB_ROOM).update(RoomDBinit.TABLE_ROOMS,cv, RoomDBinit._ID + "=" + roomPosition, null);
                updatedRooms++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return updatedRooms;
    }

    public static RoomItem getRoomItemForCurrentUser(String tag){
        Cursor cursor = null;
        try {
            if (tag!=null){
                cursor = DbShare.getCursor(DbShare.DB_ROOM,
                        RoomDBinit.TABLE_ROOMS,
                        null,
                        RoomDBinit.COLUMN_TAG + " =?",
                        new String[]{tag},
                        null,
                        null,
                        "1");
                if (cursor.getCount() > 0){
                    cursor.moveToFirst();
                    return new RoomItem().setAuditroom(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)))
                            .setStatus(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS)))
                            .setAccessType(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_ACCESS)))
                            .setLastVisiter(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER)))
                            .setPositionInBase(cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_POSITION_IN_BASE)))
                            .setTag(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG))); /* photo is null for speed*/
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

    public static void deleteFromRoomsDB(String aud){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    new String[]{RoomDBinit._ID, RoomDBinit.COLUMN_ROOM},
                    null,
                    null,
                    null,
                    null,
                    null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                if (aud.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)))){
                    DbShare.getDataBase(DbShare.DB_ROOM).delete(RoomDBinit.TABLE_ROOMS,
                            RoomDBinit._ID + "=" + cursor.getString(cursor.getColumnIndex(RoomDBinit._ID)),
                            null);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<RoomItem> readRoomsDB(){
        Cursor cursor = null;
        try {
            ArrayList<RoomItem> roomItems = new ArrayList<>();
            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                roomItems.add(new RoomItem().setAuditroom(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)))
                        .setStatus(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS)))
                        .setTag(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_ACCESS)))
                        .setPositionInBase(cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_POSITION_IN_BASE)))
                        .setLastVisiter(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER)))
                        .setTag(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG))));
                        //.setPhoto(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_PHOTO_PATH))));
            }
            return roomItems;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }
    }

    public static RoomItem getRoomItem (String tag, String aud){
        Cursor cursor = null;
        try {
            String selection;
            String selectionArg;
            if (tag!=null){
                selection = RoomDBinit.COLUMN_TAG;
                selectionArg = tag;
            } else {
                selection = RoomDBinit.COLUMN_ROOM;
                selectionArg = aud;
            }
            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    null,
                    selection + " =?",
                    new String[]{selectionArg},
                    null,
                    null,
                    "1");
            if (cursor.getCount() > 0){
                cursor.moveToFirst();
                return new RoomItem().setAuditroom(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)))
                        .setStatus(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_ACCESS)))
                        .setPositionInBase(cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_POSITION_IN_BASE)))
                        .setLastVisiter(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER)))
                        .setTag(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG)))
                        .setPhoto(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_PHOTO_PATH)));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return null;
    }


    public static ArrayList<String> getRoomList(){
        Cursor cursor = null;
        ArrayList<String> items = new ArrayList<>();
        try {

            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    new String[]{RoomDBinit.COLUMN_ROOM},
                    null,
                    null,
                    null,
                    null,
                    null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static void backupRoomsToFile(){
        Cursor cursor = null;
        try {
            ArrayList <String> itemList = new ArrayList<>();
            FileOutputStream fileOutputStream;
            String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(mPath + "/Rooms.csv");

            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                String room = cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM));
                String status = cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS));
                String cardOrHand = cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ACCESS));
                String lastVisiter = cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER));
                String tag = cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG));
                String photoPath = cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_PHOTO_PATH));
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

    public static int closeAllRooms(){
        int closedRooms = 0;

        ArrayList<RoomItem> rooms = RoomDB.readRoomsDB();
        for (RoomItem roomItem : rooms){
            if (roomItem.getStatus() == ROOM_IS_BUSY){
                roomItem.setStatus(ROOM_IS_FREE);
                roomItem.setTag(Values.EMPTY);

                RoomDB.updateRoom(roomItem);
                JournalDB.updateDB(roomItem.getPositionInBase());

                closedRooms++;
            }
        }

        return closedRooms;
    }

    public static void clearRoomsDB(){
        try {
            DbShare.getDataBase(DbShare.DB_ROOM).delete(RoomDBinit.TABLE_ROOMS,null,null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();
    }
}
