package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.util.ArrayList;


public class DataBaseShedule {

    public DataBaseSheduleRegist dataBaseSheduleRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;

    private Context mContext;

    public DataBaseShedule (Context context){
        this.mContext = context;

        dataBaseSheduleRegist = new DataBaseSheduleRegist(mContext);
        sqLiteDatabase = dataBaseSheduleRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseSheduleRegist.TABLE_SHEDULE,null,null,null,null,null,null);
    }

    public void writeInBaseShedule(String timeStart, String timeEnd, String groupName, String teacherName, String auditroomName, String subject, String parity){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseSheduleRegist.COLUMN_TIME_START,timeStart);
        cv.put(DataBaseSheduleRegist.COLUMN_TIME_END,timeEnd);
        cv.put(DataBaseSheduleRegist.COLUMN_GROUP_NAME,groupName);
        cv.put(DataBaseSheduleRegist.COLUMN_TEACHER_NAME,teacherName);
        cv.put(DataBaseSheduleRegist.COLUMN_AUDITROOM_NAME,auditroomName);
        cv.put(DataBaseSheduleRegist.COLUMN_SUBJECT,subject);
        cv.put(DataBaseSheduleRegist.COLUMN_PARITY,parity);
        sqLiteDatabase.insert(DataBaseSheduleRegist.TABLE_SHEDULE,null,cv);
    }

    public ArrayList<SparseArray> readShedule(){
        ArrayList <SparseArray> items = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            SparseArray card = new SparseArray();
            card.put(0,cursor.getString(cursor.getColumnIndex(DataBaseSheduleRegist.COLUMN_TIME_START)));
            card.put(1,cursor.getString(cursor.getColumnIndex(DataBaseSheduleRegist.COLUMN_TIME_END)));
            card.put(2,cursor.getString(cursor.getColumnIndex(DataBaseSheduleRegist.COLUMN_GROUP_NAME)));
            card.put(3,cursor.getString(cursor.getColumnIndex(DataBaseSheduleRegist.COLUMN_TEACHER_NAME)));
            card.put(4,cursor.getString(cursor.getColumnIndex(DataBaseSheduleRegist.COLUMN_AUDITROOM_NAME)));
            card.put(5,cursor.getString(cursor.getColumnIndex(DataBaseSheduleRegist.COLUMN_SUBJECT)));
            items.add(card);
        }
        return items;
    }

    public void clearBaseShedule(){
        sqLiteDatabase.delete(DataBaseSheduleRegist.TABLE_SHEDULE,null,null);
    }

    public void closeDB(){
        sqLiteDatabase.close();
        dataBaseSheduleRegist.close();
        cursor.close();
    }
}
