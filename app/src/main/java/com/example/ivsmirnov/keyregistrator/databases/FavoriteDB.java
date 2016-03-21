package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * ДБ преподавателей
 */
public class FavoriteDB {

    public static final int LOCAL_USER = 0;
    public static final int SERVER_USER = 1;
    public static final int FULLSIZE_PHOTO = 2;
    public static final int PREVIEW_PHOTO = 3;
    public static final int ALL_PHOTO = 4;
    public static final int NO_PHOTO = 5;

    public static final int SHORT_INITIALS = 6;
    public static final int FULL_INITIALS = 7;


    public static PersonItem getPersonItem(Context mContext, String tag, int userLocation, int photoType){

        Cursor cursor = null;
        try {
            PersonItem personItem;
            if (userLocation == LOCAL_USER){
                cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                        FavoriteDBinit.TABLE_TEACHER,
                            null,
                            FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                            new String[]{tag},
                            null,
                            null,
                        "1");

                if (cursor!=null && cursor.getCount()>0){
                    cursor.moveToFirst();
                    personItem = new PersonItem().setLastname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)))
                            .setFirstname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE)))
                            .setMidname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE)))
                            .setDivision(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_DIVISION_FAVORITE)))
                            .setSex(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_SEX_FAVORITE)))
                            .setRadioLabel(tag);

                    switch (photoType){
                        case FULLSIZE_PHOTO:
                            personItem.setPhotoOriginal(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_ORIGINAL_FAVORITE)));
                            break;
                        case PREVIEW_PHOTO:
                            personItem.setPhotoPreview(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_PREVIEW_FAVORITE)));
                            break;
                        case ALL_PHOTO:
                            personItem.setPhotoOriginal(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_ORIGINAL_FAVORITE)));
                            personItem.setPhotoPreview(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_PREVIEW_FAVORITE)));
                            break;
                        case NO_PHOTO:
                            break;
                        default:
                            break;
                    }

                    return personItem;
                } else {
                    return null;
                }
            }else if (userLocation == SERVER_USER){
                Connection connection = SQL_Connection.SQLconnect;

                if (connection!=null){
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("select * from STAFF_NEW where [RADIO_LABEL] = '" + tag + "'");
                        while (resultSet.next()) {
                            personItem = new PersonItem().setLastname(resultSet.getString("LASTNAME"))
                                    .setFirstname(resultSet.getString("FIRSTNAME"))
                                    .setMidname(resultSet.getString("MIDNAME"))
                                    .setDivision(resultSet.getString("NAME_DIVISION"))
                                    .setSex(resultSet.getString("SEX"))
                                    .setRadioLabel(resultSet.getString("RADIO_LABEL"));
                            String photo = resultSet.getString("PHOTO");

                            if (photo == null)
                                photo = getBase64DefaultPhotoFromResources(mContext, resultSet.getString("SEX"));

                            switch (photoType) {
                                case FULLSIZE_PHOTO:
                                    personItem.setPhotoOriginal(photo);
                                    break;
                                case PREVIEW_PHOTO:
                                    personItem.setPhotoPreview(getPhotoPreview(photo));
                                    break;
                                case ALL_PHOTO:
                                    personItem.setPhotoOriginal(photo);
                                    personItem.setPhotoPreview(getPhotoPreview(photo));
                                    break;
                                case NO_PHOTO:
                                    break;
                                default:
                                    break;
                            }
                            return personItem;
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                }else{
                    Toast.makeText(mContext,"Нет подключения к серверу!",Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    public static void updatePersonItem(String tag, PersonItem personItem){
        Cursor cursor = null;
        try {

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_TEACHER,
                    new String[]{FavoriteDBinit._ID, FavoriteDBinit.COLUMN_LASTNAME_FAVORITE, FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE,
                            FavoriteDBinit.COLUMN_MIDNAME_FAVORITE, FavoriteDBinit.COLUMN_DIVISION_FAVORITE},
                    FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                    new String[]{tag},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                ContentValues cv = new ContentValues();
                if (!personItem.getLastname().equals(Values.EMPTY)){
                    cv.put(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE,personItem.getLastname());
                }
                if (!personItem.getFirstname().equals(Values.EMPTY)){
                    cv.put(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
                }
                if (!personItem.getMidname().equals(Values.EMPTY)){
                    cv.put(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE,personItem.getMidname());
                }
                if (!personItem.getDivision().equals(Values.EMPTY)){
                    cv.put(FavoriteDBinit.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
                }
                if (cv.size()!=0){
                    DbShare.getDataBase(DbShare.DB_FAVORITE).update(FavoriteDBinit.TABLE_TEACHER,
                            cv,
                            FavoriteDBinit._ID + "=" + cursor.getInt(cursor.getColumnIndex(FavoriteDBinit._ID)),
                            null);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
           closeCursor(cursor);
        }
    }

    public static ArrayList<String> getTagsForCurrentCharacter(String character){

        Cursor cursor = null;
        try {
            ArrayList <String> mTags = new ArrayList<>();
            String selection;
            String [] selectionArgs;
            if (character.equalsIgnoreCase("#")){
                selection = null;
                selectionArgs = null;
            } else{
                selection = FavoriteDBinit.COLUMN_LASTNAME_FAVORITE + " LIKE?";
                selectionArgs = new String[]{character + "%"};

            }
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_TEACHER,
                    new String[]{FavoriteDBinit.COLUMN_TAG_FAVORITE},
                    selection,
                    selectionArgs,
                    null,
                    FavoriteDBinit.COLUMN_LASTNAME_FAVORITE + " ASC",
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    mTags.add(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_TAG_FAVORITE)));
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

    public static ArrayList<CharacterItem> getPersonsCharacters(){

        Cursor cursor = null;
        try {
            ArrayList <CharacterItem> characters = new ArrayList<>();
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_TEACHER,
                    new String[]{FavoriteDBinit.COLUMN_LASTNAME_FAVORITE},
                    null,null,null,null,null);
            cursor.moveToPosition(-1);
            ArrayList<String> uniqueCharacters = new ArrayList<>();
            while (cursor.moveToNext()){
                String firstSymbol = cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)).substring(0,1).toUpperCase();
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
            characters.add(0,new CharacterItem().setCharacter("#").setSelection(true));
            return characters;
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            closeCursor(cursor);
        }
    }


    public static boolean isUserInBase(String tag){
        //SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_FAVORITE);
        Cursor cursor = null;
        try {
          //  /mDataBase.compileStatement("SELECT * FROM " + FavoriteDBinit.TABLE_TEACHER
            //        + " WHERE " + FavoriteDBinit.COLUMN_TAG_FAVORITE + " = '" + tag + "'").simpleQueryForString();
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_TEACHER,
                    new String[]{FavoriteDBinit.COLUMN_TAG_FAVORITE},
                    FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                    new String[]{tag},
                    null,
                    null,
                    "1");
            return cursor.getCount() > 0;
        }catch (Exception e){
            return false;
        } finally {
            closeCursor(cursor);
        }
    }

    public static boolean writeInDBTeachers(Context mContext, PersonItem personItem) {
        try {
            if (personItem!=null){

                if (personItem.getPhotoOriginal() == null){
                    personItem.setPhotoOriginal(getBase64DefaultPhotoFromResources(mContext, personItem.getSex()));
                    personItem.setPhotoPreview(getPhotoPreview(getBase64DefaultPhotoFromResources(mContext, personItem.getSex())));
                }

                ContentValues cv = new ContentValues();
                cv.put(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE, personItem.getLastname());
                cv.put(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
                cv.put(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE, personItem.getMidname());
                cv.put(FavoriteDBinit.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
                cv.put(FavoriteDBinit.COLUMN_TAG_FAVORITE, personItem.getRadioLabel());
                cv.put(FavoriteDBinit.COLUMN_SEX_FAVORITE, personItem.getSex());
                cv.put(FavoriteDBinit.COLUMN_PHOTO_PREVIEW_FAVORITE, personItem.getPhotoPreview());
                cv.put(FavoriteDBinit.COLUMN_PHOTO_ORIGINAL_FAVORITE, personItem.getPhotoOriginal());

                if (isUserInBase(personItem.getRadioLabel())){
                    deleteUser(personItem.getRadioLabel());
                }
                DbShare.getDataBase(DbShare.DB_FAVORITE).insert(FavoriteDBinit.TABLE_TEACHER, null, cv);
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void backupFavoriteStaffToFile(){
        Cursor cursor = null;

        try {
            FileOutputStream fileOutputStream;
            String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(mPath + "/Teachers.csv");

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE, FavoriteDBinit.TABLE_TEACHER, null,null,null,null,null,null);
            cursor.moveToPosition(-1);

            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                String row;
                if (cursor.getCount()!=0){
                    while (cursor.moveToNext()){

                        row = cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_DIVISION_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_SEX_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_PREVIEW_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_ORIGINAL_FAVORITE))+";"
                                +cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_TAG_FAVORITE));
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

    public static void deleteUser(String tag){

        Cursor cursor = null;
        try{

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_TEACHER,
                    new String[]{FavoriteDBinit._ID},
                    FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                    new String[]{tag},
                    null,
                    null,
                    null);

            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    //Log.d("delete", cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)));
                    DbShare.getDataBase(DbShare.DB_FAVORITE).delete(FavoriteDBinit.TABLE_TEACHER, FavoriteDBinit._ID + "=" + cursor.getInt(cursor.getColumnIndex(FavoriteDBinit._ID)), null);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }



    public static void clearTeachersDB(){
        try {
            DbShare.getDataBase(DbShare.DB_FAVORITE).delete(FavoriteDBinit.TABLE_TEACHER, null, null);
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

    public static int getPersonsCount(){

        Cursor cursor = null;
        try {

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE, FavoriteDBinit.TABLE_TEACHER, new String[]{FavoriteDBinit.COLUMN_TAG_FAVORITE},null,null,null,null,null);
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

    public static String getPersonInitials (int initialsType, String lastname, String firstname, String midname){
        String initials = Values.EMPTY;
        switch (initialsType){
            case SHORT_INITIALS:
                if (lastname.length() != 0 && firstname.length() != 1 && firstname.length()!=0 && midname.length() != 1 && midname.length() != 0) {
                    initials = lastname + " " + firstname.charAt(0) + "." + midname.charAt(0) + ".";
                } else {
                    if (lastname.length() != 1 && firstname.length() != 1) {
                        initials = lastname + " " + firstname;
                    } else {
                        if (lastname.length() != 1) {
                            initials = lastname;
                        }
                    }
                }
               break;
            case FULL_INITIALS:
                if (lastname.length() != 0 && firstname.length() != 1 && firstname.length()!=0 && midname.length() != 1 && midname.length() != 0) {
                    initials = lastname + " " + firstname + " " + midname;
                } else {
                    if (lastname.length() != 1 && firstname.length() != 1) {
                        initials = lastname + " " + firstname;
                    } else {
                        if (lastname.length() != 1) {
                            initials = lastname;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return initials;

    }

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();
    }
}
