package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * ДБ помещений
 */
public class RoomDB {

    public static final int ROOM_IS_BUSY = 0;
    public static final int ROOM_IS_FREE = 1;

    public static final String ROOMS_VALIDATE = "9LSk8wzZZlY07nC";

    public static void writeInRoomsDB (RoomItem roomItem) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(RoomDBinit.COLUMN_ROOM, roomItem.getAuditroom());
            cv.put(RoomDBinit.COLUMN_STATUS, roomItem.getStatus());
            cv.put(RoomDBinit.COLUMN_ACCESS,roomItem.getAccessType());
            cv.put(RoomDBinit.COLUMN_TIME, roomItem.getTime());
            cv.put(RoomDBinit.COLUMN_LAST_VISITER, roomItem.getLastVisiter());
            cv.put(RoomDBinit.COLUMN_TAG,roomItem.getTag());
            //cv.put(RoomDBinit.COLUMN_PHOTO_PATH, roomItem.getPhoto());
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
                cv.put(RoomDBinit.COLUMN_TIME,roomItem.getTime());
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
                            .setTime(cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_TIME)))
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
                    RoomDBinit.COLUMN_ROOM + " =?",
                    new String[]{aud},
                    null,
                    null,
                    "1");
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                DbShare.getDataBase(DbShare.DB_ROOM).delete(RoomDBinit.TABLE_ROOMS,
                            RoomDBinit._ID + "=" + cursor.getString(cursor.getColumnIndex(RoomDBinit._ID)),
                            null);
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
                        .setTime(cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_TIME)))
                        .setLastVisiter(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER)))
                        .setGridOrient(App.getAppContext().getResources().getConfiguration().orientation)
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

    public static ArrayList<String> getRoomsTags(){
        Cursor cursor = null;
        try {
            ArrayList<String> tags = new ArrayList<>();
            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    new String[]{RoomDBinit.COLUMN_TAG},
                    null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                tags.add(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG)));
            }
            return tags;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }
    }

    public static RoomItem getRoomItem (String aud){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    null,
                    RoomDBinit.COLUMN_ROOM + " =?",
                    new String[]{aud},
                    null,
                    null,
                    "1");
            if (cursor.getCount() > 0){
                cursor.moveToFirst();

                return new RoomItem().setAuditroom(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)))
                        .setStatus(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_ACCESS)))
                        .setTime(cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_TIME)))
                        .setLastVisiter(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER)))
                        .setTag(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG)));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return null;
    }

    public static int getRoomStatus(String aud){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_ROOM, RoomDBinit.TABLE_ROOMS,
                    new String[]{RoomDBinit.COLUMN_STATUS},
                    RoomDBinit.COLUMN_ROOM + " =?",
                    new String[]{aud},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                return cursor.getInt(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return -1;
    }

    public static long getRoomTimeIn (String aud){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_ROOM, RoomDBinit.TABLE_ROOMS,
                    new String[]{RoomDBinit.COLUMN_TIME},
                    RoomDBinit.COLUMN_ROOM + " =?",
                    new String[]{aud},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                return cursor.getLong(cursor.getColumnIndex(RoomDBinit.COLUMN_TIME));
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return 0;
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
            FileOutputStream fileOutputStream;
            File file = new File(Settings.getBackupLocation() + "/Rooms.csv");

            cursor = DbShare.getCursor(DbShare.DB_ROOM,
                    RoomDBinit.TABLE_ROOMS,
                    null,null,null,null,null,null);
            cursor.moveToPosition(-1);

            if (file!=null){
                fileOutputStream = new FileOutputStream(file);
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(ROOMS_VALIDATE);
                fileOutputStream.write(stringBuilder.toString().getBytes());
                fileOutputStream.write("\n".getBytes());
                stringBuilder.delete(0, stringBuilder.length());

                String delimeter = ";";
                while (cursor.moveToNext()){

                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ROOM)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_STATUS)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_ACCESS)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_LAST_VISITER)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_TAG)));
                    //stringBuilder.append(delimeter);
                    //stringBuilder.append(cursor.getString(cursor.getColumnIndex(RoomDBinit.COLUMN_PHOTO_PATH)));

                    System.out.println("string: " + stringBuilder.toString());

                    fileOutputStream.write(stringBuilder.toString().getBytes());
                    fileOutputStream.write("\n".getBytes());

                    stringBuilder.delete(0, stringBuilder.length());
                }
                fileOutputStream.close();
            }
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
                JournalDB.updateDB(roomItem.getTime(), System.currentTimeMillis());

                //if (Settings.getWriteServerStatus() &&
                //        Settings.getWriteServerItems().contains(App.getAppContext().getResources().getStringArray(R.array.shared_preferences_write_server_items_entries)[0])){
                //    new ServerWriter(new JournalItem().setTimeIn(roomItem.getTime())).execute(ServerWriter.JOURNAL_UPDATE);
                //}

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
