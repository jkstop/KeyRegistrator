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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ivsmirnov on 10.11.2015.
 */
public class DataBaseJournal{

    private Context mContext;
    public DataBaseJournalRegist dataBaseJournalRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public DataBaseJournal(Context context){
        this.mContext = context;

        dataBaseJournalRegist = new DataBaseJournalRegist(mContext);
        sqLiteDatabase = dataBaseJournalRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL, null, null, null, null, null, null);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
    }

    public void writeInDBJournal(String aud, String name, Long time, Long timePut, boolean isLoadAll){
        ContentValues cv = new ContentValues();
        cursor.moveToPosition(-1);
        cv.put(DataBaseJournalRegist.COLUMN_AUD, aud);
        cv.put(DataBaseJournalRegist.COLUMN_NAME, name);
        cv.put(DataBaseJournalRegist.COLUMN_TIME, time);
        cv.put(DataBaseJournalRegist.COLUMN_TIME_PUT, timePut);
        long id = sqLiteDatabase.insert(DataBaseJournalRegist.TABLE_JOURNAL, null, cv);

        if (!isLoadAll){
            mSharedPreferencesEditor.putInt(Values.POSITION_IN_BASE_FOR_ROOM + aud, (int) id);
            mSharedPreferencesEditor.apply();
        }
    }

    public void writeInDBJournalHeaderDate(){
        ContentValues cv = new ContentValues();
        cursor.moveToPosition(-1);
        cv.put(DataBaseJournalRegist.COLUMN_AUD, "_");
        cv.put(DataBaseJournalRegist.COLUMN_NAME, Values.showDate());
        cv.put(DataBaseJournalRegist.COLUMN_TIME, (long) 1);
        cv.put(DataBaseJournalRegist.COLUMN_TIME_PUT, (long) 1);
        sqLiteDatabase.insert(DataBaseJournalRegist.TABLE_JOURNAL, null, cv);
        mSharedPreferencesEditor.putString(Values.TODAY, Values.showDate());
        mSharedPreferencesEditor.apply();
    }

    public ArrayList<SparseArray> readJournalFromDB(){
        cursor.moveToPosition(-1);
        ArrayList <SparseArray> items = new ArrayList<>();
        while (cursor.moveToNext()){
            SparseArray <String> card = new SparseArray<>();
            card.put(0, cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD)));
            card.put(1,cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_NAME)));
            card.put(2, cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME)));
            card.put(3,cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_PUT)));
            items.add(card);
        }
        return items;
    }

    public void updateDB(int id){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseJournalRegist.COLUMN_TIME_PUT, System.currentTimeMillis());
        sqLiteDatabase.update(DataBaseJournalRegist.TABLE_JOURNAL, cv,
                DataBaseJournalRegist._ID + "=" + id, null);
    }

    public void backupJournalToFile(){
        File file = null;
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(mPath + "/Journal.txt");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            String aud = cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD));
            String name = cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_NAME));
            String time;
            if (cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME))==1){
                time = "_";
            }else{
                time = String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME))));
            }
            String timePut;
            if (cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_PUT))==1){
                timePut = "_";
            }else{
                timePut = String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_PUT))));
            }
            String stroke = aud+" "+name+" "+time+" "+timePut;
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

    public void closeDay(){
        int count = 0;
        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_PUT)) == 0) {
                updateDB(cursor.getInt(cursor.getColumnIndex(DataBaseJournalRegist._ID)));
                String aud = cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD));
                dbRooms.updateStatusRooms(mSharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud,-1),"true");
                count++;
            }
        }
        dbRooms.closeDB();
        mSharedPreferencesEditor.putInt(Values.AUTO_CLOSED_COUNT, count);
        mSharedPreferencesEditor.apply();
    }

    public void closeDB(){
        dataBaseJournalRegist.close();
        sqLiteDatabase.close();
        cursor.close();
    }

    public void clearJournalDB(){
        sqLiteDatabase.delete(DataBaseJournalRegist.TABLE_JOURNAL, null, null);
    }

    public void deleteFromDB(int id){
        cursor.moveToPosition(id);
        int row = cursor.getInt(cursor.getColumnIndex(DataBaseJournalRegist._ID));
        sqLiteDatabase.delete(DataBaseJournalRegist.TABLE_JOURNAL, DataBaseJournalRegist._ID + "=" + row, null);
    }

}
