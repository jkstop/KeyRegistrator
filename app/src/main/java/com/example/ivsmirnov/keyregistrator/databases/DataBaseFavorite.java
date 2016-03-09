package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class DataBaseFavorite {

    public static final int LOCAL_USER = 0;
    public static final int SERVER_USER = 1;
    public static final int FULLSIZE_PHOTO = 2;
    public static final int PREVIEW_PHOTO = 3;

    public DataBaseFavoriteRegist dataBaseFavoriteRegist;
    public SQLiteDatabase sqLiteDatabase;

    private Context mContext;
    private Settings mSettings;

    public DataBaseFavorite(Context context){
        this.mContext = context;
        dataBaseFavoriteRegist = new DataBaseFavoriteRegist(mContext);
        sqLiteDatabase = dataBaseFavoriteRegist.getWritableDatabase();
        mSettings = new Settings(mContext);
        Log.d("FAVORITE_DB","-------------CREATE---------------");
    }

    public PersonItem getPersonItem(String tag, int userLocation, int photoSize){
        Cursor cursor = null;
        try {
            PersonItem personItem;
            if (userLocation == LOCAL_USER){
                if (sqLiteDatabase!=null){
                    cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                            + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " =?",new String[]{tag});
                }
                if (cursor!=null && cursor.getCount()>0){
                    cursor.moveToFirst();
                    personItem = new PersonItem().setLastname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))
                            .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE)))
                            .setMidname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE)))
                            .setDivision(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE)))
                            .setSex(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE)))
                            .setRadioLabel(tag);
                    if (photoSize == FULLSIZE_PHOTO){
                        personItem.setPhotoOriginal(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)));
                    }else if (photoSize == PREVIEW_PHOTO){
                        personItem.setPhotoPreview(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE)));
                    }
                    return personItem;
                } else {
                    return null;
                }
            }else if (userLocation == SERVER_USER){
                Connection connection = /*SQL_Connector.check_sql_connection(mContext, mSettings.getServerConnectionParams());*/
                        SQL_Connection.SQLconnect;

                if (connection!=null){
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("select * from STAFF_NEW where [RADIO_LABEL] = '" + tag + "'");
                        while (resultSet.next()){
                            personItem = new PersonItem().setLastname(resultSet.getString("LASTNAME"))
                                    .setFirstname(resultSet.getString("FIRSTNAME"))
                                    .setMidname(resultSet.getString("MIDNAME"))
                                    .setDivision(resultSet.getString("NAME_DIVISION"))
                                    .setSex(resultSet.getString("SEX"))
                                    .setRadioLabel(resultSet.getString("RADIO_LABEL"));
                            String photo = resultSet.getString("PHOTO");
                            if (photo == null){
                                photo = getBase64DefaultPhotoFromResources(mContext, resultSet.getString("SEX"));
                            }
                            if (photoSize == FULLSIZE_PHOTO){
                                personItem.setPhotoOriginal(photo);
                            } else {
                                personItem.setPhotoPreview(getPhotoPreview(photo));
                            }
                            return personItem;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                }else{
                    Toast.makeText(mContext,"Нет подключения к серверу!",Toast.LENGTH_SHORT).show();
                    return new PersonItem();
                }
            }
            return new PersonItem().setRadioLabel(String.valueOf(new Random().nextLong() % (100000 - 1)) + 1);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    public void updatePersonItem(String tag, PersonItem personItem){
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT "
                            + DataBaseFavoriteRegist._ID + ","
                            + DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE + ","
                            + DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE + ","
                            + DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE + ","
                            + DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE
                            + " FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                            + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE
                            + " =?",
                    new String[]{tag});
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                ContentValues cv = new ContentValues();
                if (!personItem.getLastname().equals(Values.EMPTY)){
                    cv.put(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE,personItem.getLastname());
                }
                if (!personItem.getFirstname().equals(Values.EMPTY)){
                    cv.put(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
                }
                if (!personItem.getMidname().equals(Values.EMPTY)){
                    cv.put(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE,personItem.getMidname());
                }
                if (!personItem.getDivision().equals(Values.EMPTY)){
                    cv.put(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
                }
                if (cv.size()!=0){
                    sqLiteDatabase.update(DataBaseFavoriteRegist.TABLE_TEACHER,
                            cv,
                            DataBaseFavoriteRegist._ID + "=" + cursor.getInt(cursor.getColumnIndex(DataBaseFavoriteRegist._ID)),
                            null);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public ArrayList<String> getTagForCurrentCharacter(String character){
        Cursor cursor = null;
        try {
            ArrayList <String> mTags = new ArrayList<>();
            String selection;
            String [] selectionArgs;
            if (character.equalsIgnoreCase("Все")){
                selection = null;
                selectionArgs = null;
            } else{
                selection = DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE + " LIKE?";
                selectionArgs = new String[]{character + "%"};

            }
            cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,
                    new String[]{DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE + " ASC");
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    mTags.add(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
                }
            }
            return mTags;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }
    }

    public ArrayList<CharacterItem> getPersonsCharacters(){
        Cursor cursor = null;
        try {
            ArrayList <CharacterItem> characters = new ArrayList<>();
            cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, new String[]{DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE},null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            ArrayList<String> uniqueCharacters = new ArrayList<>();
            while (cursor.moveToNext()){
                String firstSymbol = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)).substring(0,1).toUpperCase();
                if (!uniqueCharacters.contains(firstSymbol)){
                    uniqueCharacters.add(firstSymbol);
                }
            }
            for (String character : uniqueCharacters){
                characters.add(new CharacterItem().setCharacter(character).setSelection(false));
            }
            Collections.sort(characters, new Comparator<CharacterItem>() {
                @Override
                public int compare(CharacterItem lhs, CharacterItem rhs) {
                    return lhs.getCharacter().compareTo(rhs.getCharacter());
                }
            });
            characters.add(0,new CharacterItem().setCharacter("Все").setSelection(true));
            return characters;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }
    }



    public void closeDB(){
        if (dataBaseFavoriteRegist!=null){
            dataBaseFavoriteRegist.close();
            Log.d("DBFavoriteClose!","!!!!!!!!!!!!!!!!!");
        }

        if (sqLiteDatabase!=null){
            sqLiteDatabase.close();
        }
    }

    public boolean isUserInBase(String tag){
        try {
            sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " = '" + tag + "'").simpleQueryForString();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void writeInDBTeachers(PersonItem personItem) {
        try {
            if (personItem!=null){
                if (personItem.getPhotoOriginal() == null){
                    personItem.setPhotoOriginal(getBase64DefaultPhotoFromResources(mContext, personItem.getSex()));
                    personItem.setPhotoPreview(getPhotoPreview(getBase64DefaultPhotoFromResources(mContext, personItem.getSex())));
                }
                ContentValues cv = new ContentValues();
                cv.put(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE, personItem.getLastname());
                cv.put(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
                cv.put(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE, personItem.getMidname());
                cv.put(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
                cv.put(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE, personItem.getRadioLabel());
                cv.put(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE, personItem.getSex());
                cv.put(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE, personItem.getPhotoPreview());
                cv.put(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE, personItem.getPhotoOriginal());

                if (isUserInBase(personItem.getRadioLabel())){
                    deleteUser(personItem.getRadioLabel());
                }
                sqLiteDatabase.insert(DataBaseFavoriteRegist.TABLE_TEACHER, null, cv);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void backupFavoriteStaffToFile(){
        Cursor cursor = null;
        try {
            File file = null;
            FileOutputStream fileOutputStream;
            String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(mPath + "/Teachers.csv");

            cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, null,null,null,null,null,null);
            cursor.moveToPosition(-1);

            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                String row;
                if (cursor.getCount()!=0){
                    while (cursor.moveToNext()){
                        row = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE));
                        fileOutputStream.write(row.getBytes());
                        fileOutputStream.write("\n".getBytes());
                    }
                }
                fileOutputStream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public ArrayList<PersonItem> readTeachersFromDB(){
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, new String[]{DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE,
                    DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},null,null,null,null,null,null);
            cursor.moveToPosition(-1);
            ArrayList <PersonItem> mPerson = new ArrayList<>();            while (cursor.moveToNext()){

                mPerson.add(new PersonItem()
                        .setLastname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))
                        .setRadioLabel(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE))));
            }
            Collections.sort(mPerson, new Comparator<PersonItem>() {
                @Override
                public int compare(PersonItem lhs, PersonItem rhs) {
                    return lhs.getLastname().compareToIgnoreCase(rhs.getLastname());
                }
            });
            return mPerson;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }
    }

    public void deleteUser(String tag){
        Cursor cursor = null;
        try{
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " =?",new String[]{tag});
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    Log.d("delete", cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)));
                    sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, DataBaseFavoriteRegist._ID + "=" + cursor.getInt(cursor.getColumnIndex(DataBaseFavoriteRegist._ID)), null);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

    }

    public void deleteFromTeachersDB(PersonItem personItem){
        try {
            String personPosition = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE + " = '" + personItem.getLastname() + "'"
                    + " AND " + DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE + " = '" + personItem.getFirstname() + "'"
                    + " AND " + DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE + " = '" + personItem.getMidname() + "'"
                    + " AND " + DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE + " = '" + personItem.getDivision() + "'" ).simpleQueryForString();
            if (!personPosition.isEmpty()){
                sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, DataBaseFavoriteRegist._ID + "=" + personPosition, null);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void clearTeachersDB(){
        try {
            sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String getPhotoPreview(String photo){

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

            options.inSampleSize = calculateInSampleSize(options, 240, 320);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;

            Bitmap bitmapPrew = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapPrew.compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return Base64.encodeToString(byteArray,Base64.NO_WRAP);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public int getPersonsCount(){
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, new String[]{DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},null,null,null,null,null);
            if (cursor.getCount()>0){
                return cursor.getCount();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return 0;
    }



    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static String getBase64DefaultPhotoFromResources(Context context, String sex){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap;
            if (sex.equalsIgnoreCase("М")){
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_male_colored, options);
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_female_colored, options);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray,Base64.NO_WRAP);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void closeCursor(Cursor cursor){
        if (cursor!=null){
            cursor.close();
        }
    }
}
