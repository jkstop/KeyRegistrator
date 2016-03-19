package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Created by ivsmirnov on 10.11.2015.
 */
public class JournalDB {

    public static final int ACCESS_BY_CLICK = 0;
    public static final int ACCESS_BY_CARD = 1;

    public static final int COUNT_TODAY = 2;
    public static final int COUNT_TOTAL = 3;

    public static long writeInDBJournal(JournalItem journalItem){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_JOURNAL);
        Cursor cursor = null;
        try {
            ContentValues cv = new ContentValues();
            cv.put(JournalDBinit.COLUMN_USER_ID, journalItem.getAccountID());
            cv.put(JournalDBinit.COLUMN_AUD, journalItem.getAuditroom());
            cv.put(JournalDBinit.COLUMN_TIME_IN, journalItem.getTimeIn());
            cv.put(JournalDBinit.COLUMN_TIME_OUT, journalItem.getTimeOut());
            cv.put(JournalDBinit.COLUMN_ACCESS_TYPE,journalItem.getAccessType());
            cv.put(JournalDBinit.COLUMN_PERSON_LASTNAME,journalItem.getPersonLastname());
            cv.put(JournalDBinit.COLUMN_PERSON_FIRSTNAME, journalItem.getPersonFirstname());
            cv.put(JournalDBinit.COLUMN_PERSON_MIDNAME, journalItem.getPersonMidname());
            cv.put(JournalDBinit.COLUMN_PERSON_PHOTO, journalItem.getPersonPhoto());

            long position = mDataBase.insert(JournalDBinit.TABLE_JOURNAL, null, cv);

            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    null, null, null, null, null);

            Settings.setTotalJournalCount(cursor.getCount());
            return position;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<JournalItem> getJournalItemTags(Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <JournalItem> items = new ArrayList<>();
        Cursor cursor = null;
        try {

            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    JournalDBinit.COLUMN_USER_ID + " =?",
                    new String[]{Settings.getActiveAccountID()},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))))){
                        items.add(new JournalItem()
                        .setTimeIn(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))));

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

    public static JournalItem getJournalItem(long timeIn) {
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    null,
                    JournalDBinit.COLUMN_TIME_IN + " =?",
                    new String[]{String.valueOf(timeIn)},
                    null,
                    null,
                    "1");
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                return new JournalItem().setAuditroom(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD)))
                        .setAccountID(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID)))
                        .setTimeIn(timeIn)
                        .setTimeOut(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT)))
                        .setAccessType(cursor.getInt(cursor.getColumnIndex(JournalDBinit.COLUMN_ACCESS_TYPE)))
                        .setPersonLastname(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_LASTNAME)))
                        .setPersonFirstname(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_FIRSTNAME)))
                        .setPersonMidname(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_MIDNAME)))
                        .setPersonPhoto(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_PHOTO)));
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

    public static int getItemCount(int type){
        int count = 0;
        DateFormat dateFormat = DateFormat.getDateInstance();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    JournalDBinit.COLUMN_USER_ID + " =?",
                    new String[]{Settings.getActiveAccountID()},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                if (type == COUNT_TODAY){
                    String today = dateFormat.format(new Date(System.currentTimeMillis()));
                    while (cursor.moveToNext()){
                        if (dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))))
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


    public static ArrayList<JournalItem> realAllJournalFromDB(){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    null,null,null,null,null);
            cursor.moveToPosition(-1);
            ArrayList<JournalItem> items = new ArrayList<>();
            while (cursor.moveToNext()){
                items.add(new JournalItem()
                        .setTimeIn(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))));

            }
            return items;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
           closeCursor(cursor);
        }

    }

    public static ArrayList<String> readJournalDatesFromDB(){
        DateFormat dateFormat = DateFormat.getDateInstance();
        final ArrayList <String> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    JournalDBinit.COLUMN_USER_ID + " =?",
                    new String[]{Settings.getActiveAccountID()},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    String selectedDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))));
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

        return items;
    }

    public static void updateDB(Long roomPositionInBase){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_JOURNAL);
        try {
            ContentValues cv = new ContentValues();
            cv.put(JournalDBinit.COLUMN_TIME_OUT, System.currentTimeMillis());
            mDataBase.update(JournalDBinit.TABLE_JOURNAL, cv,
                    JournalDBinit._ID + "=" + roomPositionInBase, null);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static  void backupJournalToXLS(){
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
                cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                        JournalDBinit.TABLE_JOURNAL,
                        new String[]{JournalDBinit.COLUMN_USER_ID, JournalDBinit.COLUMN_AUD, JournalDBinit.COLUMN_TIME_IN, JournalDBinit.COLUMN_TIME_OUT,
                                JournalDBinit.COLUMN_PERSON_LASTNAME, JournalDBinit.COLUMN_PERSON_FIRSTNAME, JournalDBinit.COLUMN_PERSON_MIDNAME},
                        JournalDBinit.COLUMN_USER_ID + " =?",
                        new String[]{Settings.getActiveAccountID()},
                        null,
                        null,
                        null);

                for (int i=0;i<datesString.size();i++){

                    if (cursor.getCount()!=0){
                        cursor.moveToPosition(-1);
                        int row=1;

                        WritableSheet daySheet = workbook.createSheet(datesString.get(i),i);
                        daySheet.addCell(new Label(0,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD))));
                        daySheet.addCell(new Label(1,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))));
                        daySheet.addCell(new Label(2,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT))));
                        daySheet.addCell(new Label(3,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_LASTNAME))));
                        daySheet.addCell(new Label(4,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_FIRSTNAME))));
                        daySheet.addCell(new Label(5,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_MIDNAME))));

                        while (cursor.moveToNext()){
                            if (datesString.get(i).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))))){
                                if (Settings.getActiveAccountID().equals(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID)))){
                                    daySheet.addCell(new Label(0,row,cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD))));
                                    daySheet.addCell(new Label(1,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))))));
                                    daySheet.addCell(new Label(2,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT))))));
                                    daySheet.addCell(new Label(3,row,cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_LASTNAME))));
                                    daySheet.addCell(new Label(4,row,cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_FIRSTNAME))));
                                    daySheet.addCell(new Label(5,row,cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_MIDNAME))));
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

    public static void backupJournalToCSV(){

        String fileName = "Journal.csv";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());
        File file = new File(directory,fileName);
        FileOutputStream fileOutputStream;
        Cursor cursor = null;

        cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                JournalDBinit.TABLE_JOURNAL,
                null,null,null,null,null,null);
        try {
            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                String row;
                int count = 0;
                if (cursor.getCount()!=0){
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()){
                        row = cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_ACCESS_TYPE))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_LASTNAME))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_FIRSTNAME))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_MIDNAME))+";"
                                +cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_PHOTO));
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

    /*public static JournalItem createNewItemForJournal (Context context, String personTag, String auditroom, int accessType){

        PersonItem person = FavoriteDB.getPersonItem(context, personTag, FavoriteDB.LOCAL_USER, FavoriteDB.PREVIEW_PHOTO);
        return new JournalItem().setAccountID(Settings.getActiveAccountID())
                .setAuditroom(auditroom)
                .setAccessType(accessType)
                .setTimeIn(System.currentTimeMillis())
                .setPersonLastname(person.getLastname())
                .setPersonFirstname(person.getFirstname())
                .setPersonMidname(person.getMidname())
                .setPersonPhoto(person.getPhotoPreview());
    }*/

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();

    }

    public static void clearJournalDB(){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_JOURNAL);
        try {
            mDataBase.delete(JournalDBinit.TABLE_JOURNAL, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteFromDB(long timeIn){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_JOURNAL);
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit._ID},
                    JournalDBinit.COLUMN_TIME_IN + " =?",
                    new String[]{String.valueOf(timeIn)},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                mDataBase.delete(JournalDBinit.TABLE_JOURNAL, JournalDBinit._ID + "=" + cursor.getInt(cursor.getColumnIndex(JournalDBinit._ID)), null);
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }




}
