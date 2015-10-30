package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.util.ArrayList;


public class DataBaseShedule {

    public DataBasesSheduleRegist dataBasesSheduleRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;

    private Context mContext;

    public DataBaseShedule (Context context){
        this.mContext = context;

        dataBasesSheduleRegist = new DataBasesSheduleRegist(mContext);
        sqLiteDatabase = dataBasesSheduleRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBasesSheduleRegist.TABLE_SHEDULE,null,null,null,null,null,null);
    }

    public void writeInBaseShedule(String timeStart, String timeEnd, String groupName, String teacherName, String auditroomName, String subject, String parity){
        ContentValues cv = new ContentValues();
        cv.put(DataBasesSheduleRegist.COLUMN_TIME_START,timeStart);
        cv.put(DataBasesSheduleRegist.COLUMN_TIME_END,timeEnd);
        cv.put(DataBasesSheduleRegist.COLUMN_GROUP_NAME,groupName);
        cv.put(DataBasesSheduleRegist.COLUMN_TEACHER_NAME,teacherName);
        cv.put(DataBasesSheduleRegist.COLUMN_AUDITROOM_NAME,auditroomName);
        cv.put(DataBasesSheduleRegist.COLUMN_SUBJECT,subject);
        cv.put(DataBasesSheduleRegist.COLUMN_PARITY,parity);
        sqLiteDatabase.insert(DataBasesSheduleRegist.TABLE_SHEDULE,null,cv);
    }

    public ArrayList<SparseArray> readShedule(){
        ArrayList <SparseArray> items = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            SparseArray card = new SparseArray();
            card.put(0,cursor.getString(cursor.getColumnIndex(DataBasesSheduleRegist.COLUMN_TIME_START)));
            card.put(1,cursor.getString(cursor.getColumnIndex(DataBasesSheduleRegist.COLUMN_TIME_END)));
            card.put(2,cursor.getString(cursor.getColumnIndex(DataBasesSheduleRegist.COLUMN_GROUP_NAME)));
            card.put(3,cursor.getString(cursor.getColumnIndex(DataBasesSheduleRegist.COLUMN_TEACHER_NAME)));
            card.put(4,cursor.getString(cursor.getColumnIndex(DataBasesSheduleRegist.COLUMN_AUDITROOM_NAME)));
            card.put(5,cursor.getString(cursor.getColumnIndex(DataBasesSheduleRegist.COLUMN_SUBJECT)));
            items.add(card);
        }
        return items;
    }

    public void clearBaseShedule(){
        sqLiteDatabase.delete(DataBasesSheduleRegist.TABLE_SHEDULE,null,null);
    }

    public void closeDB(){
        sqLiteDatabase.close();
        dataBasesSheduleRegist.close();
        cursor.close();
    }
}
