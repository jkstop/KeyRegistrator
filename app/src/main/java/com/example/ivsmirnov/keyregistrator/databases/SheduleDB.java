package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.util.ArrayList;


public class SheduleDB {

    public SheduleDBinit sheduleDBinit;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;

    public SheduleDB(Context context){
        sheduleDBinit = new SheduleDBinit(context);
        sqLiteDatabase = sheduleDBinit.getWritableDatabase();
        cursor = sqLiteDatabase.query(SheduleDBinit.TABLE_SHEDULE,null,null,null,null,null,null);
    }

    public void writeInBaseShedule(String timeStart, String timeEnd, String groupName, String teacherName, String auditroomName, String subject, String parity){
        ContentValues cv = new ContentValues();
        cv.put(SheduleDBinit.COLUMN_TIME_START,timeStart);
        cv.put(SheduleDBinit.COLUMN_TIME_END,timeEnd);
        cv.put(SheduleDBinit.COLUMN_GROUP_NAME,groupName);
        cv.put(SheduleDBinit.COLUMN_TEACHER_NAME,teacherName);
        cv.put(SheduleDBinit.COLUMN_AUDITROOM_NAME,auditroomName);
        cv.put(SheduleDBinit.COLUMN_SUBJECT,subject);
        cv.put(SheduleDBinit.COLUMN_PARITY,parity);
        sqLiteDatabase.insert(SheduleDBinit.TABLE_SHEDULE,null,cv);
    }
/*
    public ArrayList<SparseArray> readShedule(){
        ArrayList <SparseArray> items = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            SparseArray card = new SparseArray();
            card.put(0,cursor.getString(cursor.getColumnIndex(SheduleDBinit.COLUMN_TIME_START)));
            card.put(1,cursor.getString(cursor.getColumnIndex(SheduleDBinit.COLUMN_TIME_END)));
            card.put(2,cursor.getString(cursor.getColumnIndex(SheduleDBinit.COLUMN_GROUP_NAME)));
            card.put(3,cursor.getString(cursor.getColumnIndex(SheduleDBinit.COLUMN_TEACHER_NAME)));
            card.put(4,cursor.getString(cursor.getColumnIndex(SheduleDBinit.COLUMN_AUDITROOM_NAME)));
            card.put(5,cursor.getString(cursor.getColumnIndex(SheduleDBinit.COLUMN_SUBJECT)));
            items.add(card);
        }
        return items;
    }
*/
    public void clearBaseShedule(){
        sqLiteDatabase.delete(SheduleDBinit.TABLE_SHEDULE,null,null);
    }

    public void closeDB(){
        sqLiteDatabase.close();
        sheduleDBinit.close();
        cursor.close();
    }
}
