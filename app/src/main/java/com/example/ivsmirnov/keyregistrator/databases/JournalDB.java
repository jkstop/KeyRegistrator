package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * ДБ журнал
 */
public class JournalDB {

    public static final int COUNT_TODAY = 2;
    public static final int COUNT_TOTAL = 3;

    public static final String JOURNAL_VALIDATE = "hJU3Y5WSPQCLtvv";

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
            cv.put(JournalDBinit.COLUMN_PERSON_INITIALS,journalItem.getPersonInitials());
            cv.put(JournalDBinit.COLUMN_PERSON_TAG, journalItem.getPersonTag());

            long position = mDataBase.insert(JournalDBinit.TABLE_JOURNAL, null, cv);

            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    null, null, null, null, null);

            return position;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<Long> getJournalItemTags(Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <Long> items = new ArrayList<>();
        Cursor cursor = null;
        try {

            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    JournalDBinit.COLUMN_USER_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    JournalDBinit.COLUMN_TIME_IN,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (date!=null){ //возвращаем тэги (они же время входа) на указанную дату
                        if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))))){
                            items.add(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)));
                        }
                    } else { //если пусто, то возвращаем все
                        items.add(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)));
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

    public static ArrayList<JournalItem> getJournalItemsForCurrentDate (Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <JournalItem> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    null,
                    JournalDBinit.COLUMN_USER_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    JournalDBinit.COLUMN_TIME_IN,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (date!=null){ //если есть дата, то добавляем в список JournalItem с этой датой
                        if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))))){
                            items.add(new JournalItem()
                                    .setAccountID(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID)))
                                    .setAuditroom(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD)))
                                    .setAccessType(cursor.getInt(cursor.getColumnIndex(JournalDBinit.COLUMN_ACCESS_TYPE)))
                                    .setTimeIn(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))
                                    .setTimeOut(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT)))
                                    .setPersonInitials(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_INITIALS)))
                                    .setPersonTag(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_TAG))));
                        }
                    } else {
                        items.add(new JournalItem()
                                .setAccountID(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID)))
                                .setAuditroom(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD)))
                                .setAccessType(cursor.getInt(cursor.getColumnIndex(JournalDBinit.COLUMN_ACCESS_TYPE)))
                                .setTimeIn(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))
                                .setTimeOut(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT)))
                                .setPersonInitials(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_INITIALS)))
                                .setPersonTag(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_TAG))));
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }


    public static boolean isItemOpened(long timeIn){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN,JournalDBinit.COLUMN_TIME_OUT},
                    JournalDBinit.COLUMN_TIME_IN + " =?",
                    new String[]{String.valueOf(timeIn)},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                return cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT)) == 0;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }
        return false;
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
                        .setPersonInitials(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_INITIALS)))
                        .setPersonTag(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_TAG)));
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

    public static ArrayList<Long> getOpenRoomsTags(){
        Cursor cursor = null;
        ArrayList<Long> items = new ArrayList<>();
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    JournalDBinit.COLUMN_TIME_OUT + " =?",
                    new String[]{"0"},
                    null, null, null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)));
            }
            return items;
        }catch (Exception e){
            e.printStackTrace();
            return items;
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
                    new String[]{SharedPrefs.getActiveAccountID()},
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

    public static ArrayList<String> readJournalDatesFromDB(){
        DateFormat dateFormat = DateFormat.getDateInstance();
        final ArrayList <String> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    new String[]{JournalDBinit.COLUMN_TIME_IN},
                    JournalDBinit.COLUMN_USER_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    JournalDBinit.COLUMN_TIME_IN + " DESC",
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

        return items;
    }

    public static void updateDB(Long timeIn, Long timeOut){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_JOURNAL);
        Cursor cursor;
        try {
            cursor = DbShare.getCursor(DbShare.DB_JOURNAL, JournalDBinit.TABLE_JOURNAL,
                    new String[] {JournalDBinit._ID},
                    JournalDBinit.COLUMN_TIME_IN + " =?",
                    new String[]{String.valueOf(timeIn)},
                    null,
                    null,
                    "1");
            cursor.moveToFirst();
            ContentValues cv = new ContentValues();
            cv.put(JournalDBinit.COLUMN_TIME_OUT, timeOut);
            mDataBase.update(JournalDBinit.TABLE_JOURNAL, cv,
                    JournalDBinit._ID + "=" + cursor.getInt(cursor.getColumnIndex(JournalDBinit._ID)), null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static  void backupJournalToXLS(){

        File file = new File(SharedPrefs.getBackupLocation(),"/Journal.xls");
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
                                JournalDBinit.COLUMN_PERSON_INITIALS},
                        JournalDBinit.COLUMN_USER_ID + " =?",
                        new String[]{SharedPrefs.getActiveAccountID()},
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
                        daySheet.addCell(new Label(3,0,cursor.getColumnName(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_INITIALS))));

                        while (cursor.moveToNext()){
                            if (datesString.get(i).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)))))){
                                if (SharedPrefs.getActiveAccountID().equals(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID)))){
                                    daySheet.addCell(new Label(0,row,cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD))));
                                    daySheet.addCell(new Label(1,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN))))));
                                    daySheet.addCell(new Label(2,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT))))));
                                    daySheet.addCell(new Label(3,row,cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_INITIALS))));
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

        Cursor cursor = null;
        try {
            File file = new File(SharedPrefs.getBackupLocation(),"/Journal.csv");
            FileOutputStream fileOutputStream;

            cursor = DbShare.getCursor(DbShare.DB_JOURNAL,
                    JournalDBinit.TABLE_JOURNAL,
                    null,null,null,null,null,null);
            cursor.moveToPosition(-1);

            if (file != null) {
                fileOutputStream = new FileOutputStream(file);

                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(JOURNAL_VALIDATE);
                fileOutputStream.write(stringBuilder.toString().getBytes());
                fileOutputStream.write("\n".getBytes());
                stringBuilder.delete(0, stringBuilder.length());

                String delimeter = ";";

                while (cursor.moveToNext()){

                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_USER_ID)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_AUD)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_IN)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_TIME_OUT)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_ACCESS_TYPE)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_INITIALS)));
                    stringBuilder.append(delimeter);
                    stringBuilder.append(cursor.getString(cursor.getColumnIndex(JournalDBinit.COLUMN_PERSON_TAG)));

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

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();

    }

    public static void clear(){
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
