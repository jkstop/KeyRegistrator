package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by ivsmirnov on 10.11.2015.
 */
public class DataBaseJournal{

    private Context mContext;
    public DataBaseJournalRegist dataBaseJournalRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;
    private Settings mSettings;

    public DataBaseJournal(Context context){
        this.mContext = context;

        dataBaseJournalRegist = new DataBaseJournalRegist(mContext);
        sqLiteDatabase = dataBaseJournalRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL, null, null, null, null, null, null);
        mSettings = new Settings(mContext);
    }

    public long writeInDBJournal(JournalItem journalItem){

        ContentValues cv = new ContentValues();
        cv.put(DataBaseJournalRegist.COLUMN_USER_ID, journalItem.getAccountID());
        cv.put(DataBaseJournalRegist.COLUMN_AUD, journalItem.getAuditroom());
        cv.put(DataBaseJournalRegist.COLUMN_TIME_IN, journalItem.getTimeIn());
        cv.put(DataBaseJournalRegist.COLUMN_TIME_OUT, journalItem.getTimeOut());
        cv.put(DataBaseJournalRegist.COLUMN_ACCESS_TYPE,journalItem.getAccessType());
        cv.put(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME,journalItem.getPersonLastname());
        cv.put(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME, journalItem.getPersonFirstname());
        cv.put(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME, journalItem.getPersonMidname());
        cv.put(DataBaseJournalRegist.COLUMN_PERSON_PHOTO, journalItem.getPersonPhoto());

        return sqLiteDatabase.insert(DataBaseJournalRegist.TABLE_JOURNAL, null, cv);
    }


    public ArrayList<JournalItem> readJournalFromDB(Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <JournalItem> items = new ArrayList<>();
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseJournalRegist.TABLE_JOURNAL + " WHERE " + DataBaseJournalRegist.COLUMN_USER_ID + " =?",new String[]{mSettings.getActiveAccountID()});
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN)))))){
                        items.add(new JournalItem()
                        .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_USER_ID)))
                        .setAuditroom(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD)))
                        .setTimeIn(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN)))
                        .setTimeOut(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_OUT)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_ACCESS_TYPE)))
                        .setPersonLastname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME)))
                        .setPersonFirstname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME)))
                        .setPersonMidname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME)))
                        .setPersonPhoto(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_PHOTO))));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        return items;
    }

    public ArrayList<JournalItem> realAllJournalFromDB(){
        cursor.moveToPosition(-1);
        ArrayList<JournalItem> items = new ArrayList<>();
        while (cursor.moveToNext()){
            items.add(new JournalItem()
                    .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_USER_ID)))
                    .setAuditroom(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD)))
                    .setTimeIn(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN)))
                    .setTimeOut(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_OUT)))
                    .setAccessType(cursor.getInt(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_ACCESS_TYPE)))
                    .setPersonLastname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME)))
                    .setPersonFirstname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME)))
                    .setPersonMidname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME)))
                    .setPersonPhoto(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_PHOTO))));
        }
        return items;
    }

    public ArrayList<String> readJournalDatesFromDB(){
        DateFormat dateFormat = DateFormat.getDateInstance();
        final ArrayList <String> items = new ArrayList<>();
        try {
            cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL,new String[]{DataBaseJournalRegist.COLUMN_TIME_IN},null,null,null,null,null);
            while (cursor.moveToNext()){
                String selectedDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))));
                if (!items.contains(selectedDate)){
                    items.add(selectedDate);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }

        Collections.sort(items, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return rhs.compareTo(lhs);
            }
        });
        return items;
    }

    public void updateDB(Long roomPositionInBase){
        ContentValues cv = new ContentValues();
        cv.put(DataBaseJournalRegist.COLUMN_TIME_OUT, System.currentTimeMillis());
        sqLiteDatabase.update(DataBaseJournalRegist.TABLE_JOURNAL, cv,
                DataBaseJournalRegist._ID + "=" + roomPositionInBase, null);
    }

    public void backupJournalToXLS(){
        String fileNameXLS = "Journal"+".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());
        File file = new File(directory,fileNameXLS);

        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setLocale(new Locale("ru","RU"));
        WritableWorkbook workbook;
        try {
            workbook = Workbook.createWorkbook(file,workbookSettings);
           DateFormat dateFormat = DateFormat.getDateInstance();

            ArrayList<String> datesString = readJournalDatesFromDB();
            if (datesString.size()!=0){
                cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL,null,null,null,null,null,null);
                for (int i=0;i<datesString.size();i++){
                    cursor.moveToPosition(-1);
                    int row=1;
                    WritableSheet daySheet = workbook.createSheet(datesString.get(i),i);

                        daySheet.addCell(new Label(0,0,cursor.getColumnName(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD))));
                        daySheet.addCell(new Label(1,0,cursor.getColumnName(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))));
                        daySheet.addCell(new Label(2,0,cursor.getColumnName(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_OUT))));
                        daySheet.addCell(new Label(3,0,cursor.getColumnName(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME))));
                        daySheet.addCell(new Label(4,0,cursor.getColumnName(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME))));
                        daySheet.addCell(new Label(5,0,cursor.getColumnName(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME))));
                        while (cursor.moveToNext()){
                            if (datesString.get(i).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN)))))){
                                if (mSettings.getActiveAccountID().equals(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_USER_ID)))){
                                    daySheet.addCell(new Label(0,row,cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD))));
                                    daySheet.addCell(new Label(1,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))))));
                                    daySheet.addCell(new Label(2,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_OUT))))));
                                    daySheet.addCell(new Label(3,row,cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME))));
                                    daySheet.addCell(new Label(4,row,cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME))));
                                    daySheet.addCell(new Label(5,row,cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME))));
                                    row++;
                                }
                            }
                        }
                }
                try {
                    workbook.write();
                    workbook.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public void backupJournalToCSV(){
        String fileName = "Journal.csv";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());
        File file = new File(directory,fileName);
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;

        cursor.moveToPosition(-1);
        /*while (cursor.moveToNext()){
            itemList.add();
        }*/

        try {
            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                String row;
                int count = 0;
                while (cursor.moveToNext()){
                    row = cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_OUT))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_ACCESS_TYPE))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME))+";"
                            +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_PHOTO));
                    fileOutputStream.write(row.getBytes());
                    fileOutputStream.write("\n".getBytes());
                    count++;
                }
                Log.d("count",String.valueOf(count));
                /*for (int i = 0; i < itemList.size(); i++) {
                    fileOutputStream.write(itemList.get(i).getBytes());
                    fileOutputStream.write("\n".getBytes());

                }*/

                fileOutputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        itemList.clear();
    }
/*
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
*/
    public void closeDB(){
        dataBaseJournalRegist.close();
        sqLiteDatabase.close();
       // cursor.close();
    }

    public void clearJournalDB(){
        sqLiteDatabase.delete(DataBaseJournalRegist.TABLE_JOURNAL, null, null);
    }

    public void deleteFromDB(long timeIn){
        String itemPositionInBase = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseJournalRegist.TABLE_JOURNAL +
                " WHERE " + DataBaseJournalRegist.COLUMN_TIME_IN + " = " + timeIn).simpleQueryForString();
        sqLiteDatabase.delete(DataBaseJournalRegist.TABLE_JOURNAL, DataBaseJournalRegist._ID + "=" + itemPositionInBase, null);
    }

}
