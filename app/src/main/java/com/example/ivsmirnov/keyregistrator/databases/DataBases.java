package com.example.ivsmirnov.keyregistrator.databases;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DataBases{

    public DataBasesRegist dataBasesRegist;
    public SQLiteDatabase base;
    public Cursor cursorJournal;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private String mPath;
    private Context context;

    public DataBases(Context context){

        this.context = context;
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        dataBasesRegist = new DataBasesRegist(context);
        base = dataBasesRegist.getWritableDatabase();
        cursorJournal = base.query(DataBasesRegist.TABLE_JOURNAL, null, null, null, null, null, null);
        //cursorTeachers = base.query(DataBaseFavoriteRegist.TABLE_TEACHER, null, null, null, null, null, null);
        //cursorRoom = base.query(DataBasesRegist.TABLE_ROOMS,null,null,null,null,null,null);
        //cursorBaseSql = base.query(DataBasesRegist.TABLE_BASE,null,null,null,null,null,null);
        Log.d("DB connection is", "OPEN");
    }
/*
    public ArrayList<Integer> readFromRoomsDB (){
        cursorRoom.moveToPosition(-1);
        ArrayList<Integer> items = new ArrayList<>();
        while (cursorRoom.moveToNext()){
            items.add(cursorRoom.getInt(cursorRoom.getColumnIndex(DataBasesRegist.COLUMN_ROOM)));
        }
        return items;
    }

    public ArrayList<Boolean> readStatusRooms(){
        cursorRoom.moveToPosition(-1);
        ArrayList<Boolean>items = new ArrayList<>();
        while (cursorRoom.moveToNext()){
            if (cursorRoom.getInt(cursorRoom.getColumnIndex(DataBasesRegist.COLUMN_STATUS))==1){
                items.add(true);
            }else{
                items.add(false);
            }
        }
        return items;
    }

    public void updateStatusRooms(int id, int status){

        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_STATUS, status);
        base.update(DataBasesRegist.TABLE_ROOMS, cv, DataBasesRegist._ID + "=" + id, null);
    }

    public ArrayList<String> readAudirtoomsFromDB(){
        ArrayList<String>items = new ArrayList<>();
        cursorRoom.moveToPosition(-1);
        while (cursorRoom.moveToNext()){
            String aud = cursorRoom.getString(cursorRoom.getColumnIndex(DataBasesRegist.COLUMN_ROOM));
            items.add(aud);
        }
        return items;
    }
/*
    public String findPhotoPath(String[] items){
        String path = "";
        cursorTeachers.moveToPosition(-1);
        while (cursorTeachers.moveToNext()){
            if (items[0].equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_SURNAME_FAVORITE)))&&
                    items[1].equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_NAME_FAVORITE)))&&
                    items[2].equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    items[3].equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_KAF_FAVORITE)))){
                path = cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_PHOTO_FAVORITE));
            }
        }
        return path;
    }
*/


 /*   public ArrayList<String> readLastVisiterRoom(){
        cursorRoom.moveToPosition(-1);
        ArrayList<String>items = new ArrayList<>();
        while (cursorRoom.moveToNext()){
            items.add(cursorRoom.getString(cursorRoom.getColumnIndex(DataBasesRegist.COLUMN_LAST_VISITER)));
        }
        return items;
    }

    public void updateLastVisitersRoom(int id,String name){
        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_LAST_VISITER, name);
        base.update(DataBasesRegist.TABLE_ROOMS, cv, DataBasesRegist._ID + "=" + id, null);

        Log.d("updateLastVisiters", "OK");
    }

    public void updatePhotoPath(int id, String path){
        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_PHOTO_PATH, path);
        base.update(DataBasesRegist.TABLE_ROOMS, cv, DataBasesRegist._ID + "=" + id, null);

        Log.d("UpdatePhotoPath", "OK");
    }

    public ArrayList<String> readPhotoPath(){
        ArrayList <String> items = new ArrayList<>();
        cursorRoom.moveToPosition(-1);
        while (cursorRoom.moveToNext()){
            items.add(cursorRoom.getString(cursorRoom.getColumnIndex(DataBasesRegist.COLUMN_PHOTO_PATH)));
        }
        return items;
    }
/*
    public void writeInRoomsDB (int room) {
        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_ROOM, room);
        cv.put(DataBasesRegist.COLUMN_STATUS,1);
        cv.put(DataBasesRegist.COLUMN_LAST_VISITER, "Аноним");
        cv.put(DataBasesRegist.COLUMN_PHOTO_PATH, "");
        long id = base.insert(DataBasesRegist.TABLE_ROOMS, null, cv);

        editor.putInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + String.valueOf(room), (int) id);
        editor.commit();
    }

    public void deleteFromRoomsDB(int id){
        base.delete(DataBasesRegist.TABLE_ROOMS, DataBasesRegist._ID + "=" + id, null);
    }
*/
    public void deleteFromDB(int id){

        cursorJournal.moveToPosition(id);
        int row = cursorJournal.getInt(cursorJournal.getColumnIndex(DataBasesRegist._ID));
        base.delete(DataBasesRegist.TABLE_JOURNAL, DataBasesRegist._ID + "=" + row, null);

    }

    //запись в БД преподавателей
   /* public void writeInDBTeachers(String surname, String name, String lastname, String kaf, String gender, String photo, String oroginalPhoto) {
        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_SURNAME_FAVORITE,surname);
        cv.put(DataBasesRegist.COLUMN_NAME_FAVORITE, name);
        cv.put(DataBasesRegist.COLUMN_LASTNAME_FAVORITE,lastname);
        cv.put(DataBasesRegist.COLUMN_KAF_FAVORITE, kaf);
        cv.put(DataBasesRegist.COLUMN_GENDER_FAVORITE, gender);
        cv.put(DataBasesRegist.COLUMN_PHOTO_FAVORITE, photo);
        cv.put(DataBasesRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE, oroginalPhoto);
        long id = base.insert(DataBasesRegist.TABLE_TEACHER, null, cv);
        editor.putLong("id",id);
        editor.commit();
        Log.d("Write in Teachers DB", "OK");
    }

    //чтение из БД преподавателей
    public ArrayList<SparseArray> readTeachersFromDB(){
        cursorTeachers.moveToPosition(-1);
        ArrayList <SparseArray> items = new ArrayList<>();

        while (cursorTeachers.moveToNext()){
            SparseArray<String> card = new SparseArray<>();
            card.put(0, cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_SURNAME_FAVORITE)));
            card.put(1, cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_NAME_FAVORITE)));
            card.put(2, cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_LASTNAME_FAVORITE)));
            card.put(3, cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_KAF_FAVORITE)));
            card.put(4, cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_GENDER_FAVORITE)));
            card.put(5,cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_PHOTO_FAVORITE)));
            card.put(6, cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)));

            items.add(card);

        }

        return items;
    }
*/ //переносить отсюда в новый класс
    public ArrayList<SparseArray> readJournalFromDB(){
        cursorJournal.moveToPosition(-1);
        ArrayList <SparseArray> items = new ArrayList<>();

        while (cursorJournal.moveToNext()){
            SparseArray <String> card = new SparseArray<>();
            card.put(0,cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_AUD)));
            card.put(1,cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_NAME)));
            card.put(2,cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME)));
            card.put(3,cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME_PUT)));
            items.add(card);
        }
        return items;
    }

    //удаление из БД преподавателей
/*    public void deleteFromTeachersDB(String surname, String name,String lastname,String kaf){

        cursorTeachers.moveToPosition(-1);
        while (cursorTeachers.moveToNext()){
            if (surname.equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_SURNAME_FAVORITE)))&&
                    name.equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_NAME_FAVORITE)))&&
                    lastname.equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    kaf.equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_KAF_FAVORITE)))){
                int row = cursorTeachers.getInt(cursorTeachers.getColumnIndex(DataBasesRegist._ID));
                base.delete(DataBasesRegist.TABLE_TEACHER,DataBasesRegist._ID + "=" + row,null);
            }
        }
    }
    //очистка БД преподавателей
    public void clearTeachersDB(){
        base.delete(DataBasesRegist.TABLE_TEACHER, null, null);
        Log.d("Clear Teachers DB", "OK");
    }
*/
    public void clearJournalDB(){
        base.delete(DataBasesRegist.TABLE_JOURNAL, null, null);
        Log.d("Clear Journal DB", "OK");
    }
/*
    public void clearBaseSQL(){
        base.delete(DataBasesRegist.TABLE_BASE, null, null);
        Log.d("Clear DB", "OK");
    }
*/

/*
    public void writeInDBSQL(String kaf,String surname,String name,String lastname, String photo){
        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_KAF,kaf);
        cv.put(DataBasesRegist.COLUMN_IMYA,name);
        cv.put(DataBasesRegist.COLUMN_FAMILIA, surname);
        cv.put(DataBasesRegist.COLUMN_OTCHESTVO, lastname);
        cv.put(DataBasesRegist.COLUMN_PHOTO, photo);
        base.insert(DataBasesRegist.TABLE_BASE, null, cv);
    }

    public String findPhotoByPosition(int position){
        String photo = "null";
        cursorBaseSql = base.query(DataBasesRegist.TABLE_BASE,null,null,null,null,null,null);
        if (position !=-1){
            cursorBaseSql.moveToPosition(position);
            photo = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(DataBasesRegist.COLUMN_PHOTO));
        }
        return photo;
    }
/*
    public void writeCardInBase(String surname, String name, String lastname, String kaf, String gender, int position, String photo) {
        //cursorBaseSql = base.query(DataBasesRegist.TABLE_BASE, null, null, null, null, null, null);
        //String photo = "null";
        //if (position != -1) {
        //    cursorBaseSql.moveToPosition(position);
        //    photo = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(DataBasesRegist.COLUMN_PHOTO));
       // }

        String photoPath = savePhotoToSD(photo,surname+"_"+name+"_"+lastname);
        String originalPath = saveOriginalPhoto(photo, surname + "_" + name + "_" + lastname);
        writeInDBTeachers(surname, name, lastname, kaf, gender, photoPath, originalPath);
    }
*/
/*
    public String getPhotoID(SparseArray<String> items){
        String photoID = "null";
        cursorTeachers.moveToPosition(-1);
        while (cursorTeachers.moveToNext()){
            if (items.get(0).equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_SURNAME_FAVORITE)))&&
                    items.get(1).equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_NAME_FAVORITE)))&&
                    items.get(2).equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    items.get(3).equalsIgnoreCase(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_KAF_FAVORITE)))){
                photoID = cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_PHOTO_FAVORITE));
            }
        }

        return photoID;
    }
*/
  /*  public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }*/

  /*  public String saveOriginalPhoto(String photo, String filename) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/KeyRegistrator/Sources_photo");
        if (!folder.exists()) {
            folder.mkdir();
        }
        String photoPath = folder.getAbsolutePath() + "/" + filename + ".png";
        if (!photo.equalsIgnoreCase("null")) {
            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            Bitmap bitmapOrigin = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(photoPath);
                bitmapOrigin.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Bitmap bitmap;
            if (filename.substring(filename.length() - 1).equalsIgnoreCase("а")) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_female_colored);
            } else {
                String[] splitted = filename.split("_");
                if (splitted[0].substring(splitted[0].length() - 1).equalsIgnoreCase("а")) {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_female_colored);
                } else {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_male_colored);
                }
            }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(photoPath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return photoPath;
    }

    public String savePhotoToSD(String photo, String filename){

        File folder = new File(Environment.getExternalStorageDirectory() + "/KeyRegistrator");

        if (!folder.exists()){
            folder.mkdir();
        }

        String photoPath = folder.getAbsolutePath() + "/" + filename + ".webp";

        if (!photo.equalsIgnoreCase("null")){

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

            options.inSampleSize = calculateInSampleSize(options, 120, 160);
            options.inJustDecodeBounds = false;

            Bitmap bitmapPrew = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(photoPath);
                bitmapPrew.compress(Bitmap.CompressFormat.WEBP, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Bitmap bitmap;
            if (filename.substring(filename.length()-1).equalsIgnoreCase("а")){
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_female_colored);
            }else{
                String[] splitted = filename.split("_");
                if (splitted[0].substring(splitted[0].length() - 1).equalsIgnoreCase("а")) {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_female_colored);
                } else {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_male_colored);
                }
            }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(photoPath);
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }
        return photoPath;
    }

    public ArrayList <String> readFromBaseSQL(String column){
        ArrayList <String> items = new ArrayList<>();
        cursorBaseSql = base.query(DataBasesRegist.TABLE_BASE,new String[]{column},null,null,null,null,null);
        cursorBaseSql.moveToPosition(-1);
        while (cursorBaseSql.moveToNext()){
            String row = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(column));
            items.add(row);
        }
        return items;
    }

    public ArrayList<String> readSQL(){
        ArrayList<String> items = new ArrayList<>();
        cursorBaseSql = base.query(DataBasesRegist.TABLE_BASE,new String[]{DataBasesRegist.COLUMN_FAMILIA,DataBasesRegist.COLUMN_IMYA,DataBasesRegist.COLUMN_OTCHESTVO,DataBasesRegist.COLUMN_KAF},null,null,null,null,null);
        cursorBaseSql.moveToPosition(-1);
        while (cursorBaseSql.moveToNext()){
            String surname = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(DataBasesRegist.COLUMN_FAMILIA));
            String name = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(DataBasesRegist.COLUMN_IMYA));
            String lastname = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(DataBasesRegist.COLUMN_OTCHESTVO));
            String kaf = cursorBaseSql.getString(cursorBaseSql.getColumnIndex(DataBasesRegist.COLUMN_KAF));
            String position = String.valueOf(cursorBaseSql.getPosition());
            items.add(surname + ";" + name + ";" + lastname + ";" + kaf + ";" + position);
        }
        return items;
    }
*/

    //обновление БД преподавателей
  /*  public void updateTeachersDB(String [] source, String [] edited){

        cursorTeachers.moveToPosition(-1);
        while (cursorTeachers.moveToNext()){
            if (source[0].equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_SURNAME_FAVORITE)))&&
                    source[1].equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_NAME_FAVORITE)))&&
                    source[2].equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    source[3].equals(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBasesRegist.COLUMN_KAF_FAVORITE)))){
                int row = cursorTeachers.getInt(cursorTeachers.getColumnIndex(DataBasesRegist._ID));
                ContentValues cv = new ContentValues();
                cv.put(DataBasesRegist.COLUMN_SURNAME_FAVORITE,edited[0]);
                cv.put(DataBasesRegist.COLUMN_NAME_FAVORITE,edited[1]);
                cv.put(DataBasesRegist.COLUMN_LASTNAME_FAVORITE,edited[2]);
                cv.put(DataBasesRegist.COLUMN_KAF_FAVORITE,edited[3]);
                base.update(DataBasesRegist.TABLE_TEACHER, cv, DataBasesRegist._ID + "=" + row, null);
            }
        }
    }*/

    //запись в БД журнала
    public void writeInDBJournal(String aud, String name, Long time, Long timePut, boolean isLoadAll){
        ContentValues cv = new ContentValues();

        cursorJournal.moveToPosition(-1);

        cv.put(DataBasesRegist.COLUMN_AUD, aud);
        cv.put(DataBasesRegist.COLUMN_NAME, name);
        cv.put(DataBasesRegist.COLUMN_TIME, time);
        cv.put(DataBasesRegist.COLUMN_TIME_PUT, timePut);
        long id = base.insert(DataBasesRegist.TABLE_JOURNAL, null, cv);

        if (!isLoadAll){
            editor.putInt(Values.POSITION_IN_BASE_FOR_ROOM + aud, (int) id);
            editor.commit();
        }
    }

    //изменение записи журнала в БД
    public void updateDB(int id){
        ContentValues cv = new ContentValues();
        cv.put(DataBasesRegist.COLUMN_TIME_PUT, getTime());
        base.update(DataBasesRegist.TABLE_JOURNAL, cv,
                DataBasesRegist._ID + "=" + id, null);
        Log.d("db Update", "ok");
    }

    //запись в БД журнала даты
    public void writeInDBJournalHeaderDate(){
        ContentValues cv = new ContentValues();
        cursorJournal.moveToPosition(-1);
        cv.put(DataBasesRegist.COLUMN_AUD, "_");
        cv.put(DataBasesRegist.COLUMN_NAME, showDate());
        cv.put(DataBasesRegist.COLUMN_TIME, (long) 1);
        cv.put(DataBasesRegist.COLUMN_TIME_PUT, (long) 1);
        base.insert(DataBasesRegist.TABLE_JOURNAL, null, cv);

        editor.putString(Values.TODAY, showDate());
        editor.commit();
    }

    //запись в файл
    public void writeFile(int id) {
        int count = 0;
        File file = null;
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;
        switch (id){
            case Values.WRITE_JOURNAL:
                file = new File(mPath + "/Journal.txt");
                cursorJournal.moveToPosition(-1);
                while (cursorJournal.moveToNext()){
                    String aud = cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_AUD));
                    String name = cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_NAME));
                    String time;
                    if (cursorJournal.getLong(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME))==1){
                        time = "_";
                    }else{
                        time = String.valueOf(new Time(cursorJournal.getLong(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME))));
                    }
                    String timePut;
                    if (cursorJournal.getLong(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME_PUT))==1){
                        timePut = "_";
                    }else{
                        timePut = String.valueOf(new Time(cursorJournal.getLong(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME_PUT))));
                    }

                    String stroke = aud+" "+name+" "+time+" "+timePut;
                    itemList.add(stroke);
                }
                break;
            /*case Values.WRITE_TEACHERS:
                file = new File(mPath + "/Teachers.csv");
                cursorTeachers.moveToPosition(-1);
                while (cursorTeachers.moveToNext()){
                    File f = new File(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)));
                    Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
                    itemList.add(cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE))+";"
                    +cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE))+";"
                    +cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE))+";"
                    +cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE))+";"
                    +cursorTeachers.getString(cursorTeachers.getColumnIndex(DataBaseFavoriteRegist.COLUMN_GENDER_FAVORITE))+";"
                            + encoded);
                }
                break;*/
        }

        try{
            if (file!=null){
                fileOutputStream = new FileOutputStream(file);
                for (int i=0;i<itemList.size();i++){
                    fileOutputStream.write(itemList.get(i).getBytes());
                    fileOutputStream.write("\n".getBytes());
                    count++;
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
        DataBaseRooms dbRooms = new DataBaseRooms(context);
        cursorJournal.moveToPosition(-1);
        while (cursorJournal.moveToNext()){
            if (cursorJournal.getLong(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME_PUT)) == 0) {
                updateDB(cursorJournal.getInt(cursorJournal.getColumnIndex(DataBasesRegist._ID)));
                String aud = cursorJournal.getString(cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_AUD));
                dbRooms.updateStatusRooms(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud,-1),"true");
                count++;
            }
        }
        dbRooms.closeDB();
        editor.putInt(Values.AUTO_CLOSED_COUNT, count);
        editor.commit();
    }

    //дата для заголовка
    public static String showDate() {
        Date currentDate =  new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM",new Locale("ru"));
        return String.valueOf(dateFormat.format(currentDate));
    }

    public static long getTime(){
        return System.currentTimeMillis();
    }


    public void closeDBconnection(){
        base.close();
        dataBasesRegist.close();
        cursorJournal.close();
        //cursorTeachers.close();
        //cursorRoom.close();
//        cursorBaseSql.close();
        Log.d("DB connection is", "CLOSE");
    }

    public static void copyfile(Context context, String srFile, String dtFile){
        try{

            File f1 = new File(srFile);
            File f2 = new File(dtFile);
            InputStream in = new FileInputStream(f1);
            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

//            Toast.makeText(context,"Файл "+srFile+" скопирован в "+dtFile,Toast.LENGTH_LONG).show();
        }
        catch(FileNotFoundException ex){
            System.out.println(ex.getMessage() + " in the specified directory.");
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }


}
