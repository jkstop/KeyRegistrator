package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

import com.example.ivsmirnov.keyregistrator.async_tasks.Loader;

/**
 * Created by ivsmirnov on 03.11.2015.
 */
public class DataBaseStaff {

    private Context mContext;
    public DataBasesStaffRegist dataBasesStaffRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;

    public DataBaseStaff(Context context){
        this.mContext = context;

        dataBasesStaffRegist = new DataBasesStaffRegist(mContext);
        sqLiteDatabase = dataBasesStaffRegist.getReadableDatabase();
        cursor = sqLiteDatabase.query(DataBasesStaffRegist.TABLE_STAFF,null,null,null,null,null,null);
    }

    public void writeInBaseStaff(String nameDivision, String namePosition,String lastname, String firstname, String midname, String sex, String photo, String radiolabel){
        ContentValues cv = new ContentValues();
        cv.put(DataBasesStaffRegist.COLUMN_NAME_DIVISION,nameDivision);
        cv.put(DataBasesStaffRegist.COLUMN_NAME_POSITION,namePosition);
        cv.put(DataBasesStaffRegist.COLUMN_LASTNAME,lastname);
        cv.put(DataBasesStaffRegist.COLUMN_FIRSTNAME,firstname);
        cv.put(DataBasesStaffRegist.COLUMN_MIDNAME,midname);
        cv.put(DataBasesStaffRegist.COLUMN_SEX,sex);
        cv.put(DataBasesStaffRegist.COLUMN_PHOTO,photo);
        cv.put(DataBasesStaffRegist.COLUMN_RADIO_LABEL,radiolabel);
        sqLiteDatabase.insert(DataBasesStaffRegist.TABLE_STAFF,null,cv);
    }

    public void clearBaseStaff(){
        sqLiteDatabase.delete(DataBasesStaffRegist.TABLE_STAFF, null, null);
    }

    public void closeDB(){
        sqLiteDatabase.close();
        dataBasesStaffRegist.close();
        cursor.close();
    }

    public SparseArray<String> findUserByTag(String tag){
        SparseArray<String> items = new SparseArray<>();
        cursor = sqLiteDatabase.query(DataBasesStaffRegist.TABLE_STAFF,new String[]{DataBasesStaffRegist.COLUMN_RADIO_LABEL},null,null,null,null,null);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (tag.equals(cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_RADIO_LABEL)))){
                int position = cursor.getPosition();
                cursor = sqLiteDatabase.query(DataBasesStaffRegist.TABLE_STAFF,null,null,null,null,null,null);
                cursor.moveToPosition(position);
                DataBases db = new DataBases(mContext);
                String photoPath = db.saveOriginalPhoto(cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_LASTNAME)) + "_" + cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_FIRSTNAME))
                                + "_" + cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_MIDNAME)));
                items.put(0, cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_NAME_DIVISION)));
                items.put(1,cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_NAME_POSITION)));
                items.put(2,cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_LASTNAME)));
                items.put(3,cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_FIRSTNAME)));
                items.put(4,cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_MIDNAME)));
                items.put(5, cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_SEX)));
                items.put(6, photoPath);
                items.put(7, cursor.getString(cursor.getColumnIndex(DataBasesStaffRegist.COLUMN_RADIO_LABEL)));
                break;
            }
        }
        if (items.size()==0){
            DataBases db = new DataBases(mContext);
            String photoPath = db.saveOriginalPhoto("null", tag);
            items.put(0, "-");
            items.put(1,"-");
            items.put(2,"Аноним");
            items.put(3,"Аноним");
            items.put(4,"Аноним");
            items.put(5, "-");
            items.put(6, photoPath);
            items.put(7, tag);
        }
        return items;
    }
}
