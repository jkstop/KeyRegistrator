package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
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

    public static final int ACCESS_BY_CLICK = 0;
    public static final int ACCESS_BY_CARD = 1;

    public static final int COUNT_TODAY = 2;
    public static final int COUNT_TOTAL = 3;

    private Context mContext;
    public DataBaseJournalRegist dataBaseJournalRegist;
    public SQLiteDatabase sqLiteDatabase;
    private Settings mSettings;

    public DataBaseJournal(Context context){
        this.mContext = context;

        dataBaseJournalRegist = new DataBaseJournalRegist(mContext);
        sqLiteDatabase = dataBaseJournalRegist.getWritableDatabase();
        mSettings = new Settings(mContext);
        Log.d("JOURNAL_DB","-------------CREATE---------------");
    }

    public long writeInDBJournal(JournalItem journalItem){
        Cursor cursor = null;
        try {
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

            long position = sqLiteDatabase.insert(DataBaseJournalRegist.TABLE_JOURNAL, null, cv);

            cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL,new String[]{DataBaseJournalRegist.COLUMN_TIME_IN},null,null,null,null,null);
            mSettings.setTotalJournalCount(cursor.getCount());
            return position;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }finally {
            closeCursor(cursor);
        }
    }

    public ArrayList<JournalItem> getJournalItemTags(Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <JournalItem> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT " + DataBaseJournalRegist.COLUMN_TIME_IN
                    + " FROM " + DataBaseJournalRegist.TABLE_JOURNAL
                    + " WHERE " + DataBaseJournalRegist.COLUMN_USER_ID + " =?",
                    new String[]{mSettings.getActiveAccountID()});
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN)))))){
                        items.add(new JournalItem()
                        .setTimeIn(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))));

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }
        return items;
    }

    public JournalItem getJournalItem(long timeIn) {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseJournalRegist.TABLE_JOURNAL
                            + " WHERE " + DataBaseJournalRegist.COLUMN_TIME_IN + " =?",
                    new String[]{String.valueOf(timeIn)});
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                return new JournalItem().setAuditroom(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD)))
                        .setAccountID(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_USER_ID)))
                        .setTimeIn(timeIn)
                        .setTimeOut(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_OUT)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_ACCESS_TYPE)))
                        .setPersonLastname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_LASTNAME)))
                        .setPersonFirstname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME)))
                        .setPersonMidname(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_MIDNAME)))
                        .setPersonPhoto(cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_PERSON_PHOTO)));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    public int getItemCount(int type){
        int count = 0;
        DateFormat dateFormat = DateFormat.getDateInstance();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT " + DataBaseJournalRegist.COLUMN_TIME_IN +" FROM " + DataBaseJournalRegist.TABLE_JOURNAL
                    + " WHERE " + DataBaseJournalRegist.COLUMN_USER_ID + " =?",
                    new String[]{mSettings.getActiveAccountID()});
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                if (type == COUNT_TODAY){
                    String today = dateFormat.format(new Date(System.currentTimeMillis()));
                    while (cursor.moveToNext()){
                        if (dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))))
                                .equals(today)){
                            count++;
                        }
                    }
                } else {
                    count = cursor.getCount();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           closeCursor(cursor);
        }
        return count;
    }


    public ArrayList<JournalItem> realAllJournalFromDB(){
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL,new String[]{DataBaseJournalRegist.COLUMN_TIME_IN},null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            ArrayList<JournalItem> items = new ArrayList<>();
            while (cursor.moveToNext()){
                items.add(new JournalItem()
                        .setTimeIn(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))));

            }
            return items;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
           closeCursor(cursor);
        }

    }

    public ArrayList<String> readJournalDatesFromDB(){
        DateFormat dateFormat = DateFormat.getDateInstance();
        final ArrayList <String> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT " + DataBaseJournalRegist.COLUMN_USER_ID + ","
                    + DataBaseJournalRegist.COLUMN_TIME_IN + " FROM "
                    + DataBaseJournalRegist.TABLE_JOURNAL + " WHERE "
                    + DataBaseJournalRegist.COLUMN_USER_ID + " =?",
                    new String[]{mSettings.getActiveAccountID()});
            Log.d("CURSOR_OPEN",cursor.toString());
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    String selectedDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_TIME_IN))));
                    if (!items.contains(selectedDate)){
                        items.add(selectedDate);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }

        Collections.reverse(items);
        //Collections.sort(items, new Comparator<String>() {
         //   @Override
         //   public int compare(String lhs, String rhs) {
        //        return rhs.compareTo(lhs);
        //    }
        //});
        return items;
    }

    public void updateDB(Long roomPositionInBase){
        try {
            ContentValues cv = new ContentValues();
            cv.put(DataBaseJournalRegist.COLUMN_TIME_OUT, System.currentTimeMillis());
            sqLiteDatabase.update(DataBaseJournalRegist.TABLE_JOURNAL, cv,
                    DataBaseJournalRegist._ID + "=" + roomPositionInBase, null);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void backupJournalToXLS(){
        String fileNameXLS = "Journal"+".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());
        File file = new File(directory,fileNameXLS);
        Cursor cursor = null;

        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setLocale(new Locale("ru","RU"));
        WritableWorkbook workbook;
        try {
            ArrayList<String> datesString = readJournalDatesFromDB();

            workbook = Workbook.createWorkbook(file,workbookSettings);
           DateFormat dateFormat = DateFormat.getDateInstance();

            if (datesString.size()!=0){

                cursor = sqLiteDatabase.rawQuery("SELECT " + DataBaseJournalRegist.COLUMN_USER_ID + ","
                                + DataBaseJournalRegist.COLUMN_AUD + ","
                                + DataBaseJournalRegist.COLUMN_TIME_IN + ","
                                + DataBaseJournalRegist.COLUMN_TIME_OUT + ","
                                + DataBaseJournalRegist.COLUMN_PERSON_LASTNAME + ","
                                + DataBaseJournalRegist.COLUMN_PERSON_FIRSTNAME + ","
                                + DataBaseJournalRegist.COLUMN_PERSON_MIDNAME + " FROM "
                                + DataBaseJournalRegist.TABLE_JOURNAL + " WHERE "
                                + DataBaseJournalRegist.COLUMN_USER_ID + " =?",
                        new String[]{mSettings.getActiveAccountID()});

                for (int i=0;i<datesString.size();i++){
                    Log.d("cursor"+String.valueOf(i),String.valueOf(cursor));

                    if (cursor.getCount()!=0){
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
                }
                try {
                    workbook.write();
                    workbook.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);

        }
    }

    public void backupJournalToCSV(){

        String fileName = "Journal.csv";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());
        File file = new File(directory,fileName);
        FileOutputStream fileOutputStream;
        Cursor cursor = null;

        cursor = sqLiteDatabase.query(DataBaseJournalRegist.TABLE_JOURNAL,null,null,null,null,null,null);
        try {
            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                String row;
                int count = 0;
                if (cursor.getCount()!=0){
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()){
                        row = cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_USER_ID))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseJournalRegist.COLUMN_AUD))+";"
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
                }

                fileOutputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public void closeDB(){
        dataBaseJournalRegist.close();
        sqLiteDatabase.close();
    }

    private void closeCursor(Cursor cursor){
        if (cursor!=null){
            cursor.close();
        }
    }

    public void clearJournalDB(){
        try {
            sqLiteDatabase.delete(DataBaseJournalRegist.TABLE_JOURNAL, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void deleteFromDB(long timeIn){
        try {
            String itemPositionInBase = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseJournalRegist.TABLE_JOURNAL +
                    " WHERE " + DataBaseJournalRegist.COLUMN_TIME_IN + " = " + timeIn).simpleQueryForString();
            sqLiteDatabase.delete(DataBaseJournalRegist.TABLE_JOURNAL, DataBaseJournalRegist._ID + "=" + itemPositionInBase, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
