package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.ImageSaver;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * ДБ преподавателей
 */
public class FavoriteDB {

    public static final int LOCAL_USER = 1;
    public static final int SERVER_USER = 2;
    public static final int FULLSIZE_PHOTO = 3;
    public static final int PREVIEW_PHOTO = 4;
    public static final int ALL_PHOTO = 5;
    public static final int NO_PHOTO = 6;

    public static final int LOCAL_PHOTO = 7;
    public static final int SERVER_PHOTO = 8;

    public static final int SHORT_INITIALS = 9;
    public static final int FULL_INITIALS = 10;

    public static final int CLICK_USER_ACCESS = 100;
    public static final int CARD_USER_ACCESS = 101;


    public static final String PERSONS_VALIDATE = "H4KkNBL1EIzJKoN";

    public static PersonItem getPersonItem(String tag, boolean withBase64Photo){

        Cursor cursor = null;
        try {
            PersonItem personItem;

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                        FavoriteDBinit.TABLE_PERSONS,
                            null,
                            FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                            new String[]{tag},
                            null,
                            null,
                        "1");

            if (cursor!=null && cursor.getCount()>0){ //пользователь найден, возвращаем его
                    cursor.moveToFirst();
                    personItem = new PersonItem()
                            .setLastname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)))
                            .setFirstname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE)))
                            .setMidname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE)))
                            .setDivision(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_DIVISION_FAVORITE)))
                            .setSex(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_SEX_FAVORITE)))
                            .setAccessType(cursor.getInt(cursor.getColumnIndex(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE)))
                            .setRadioLabel(tag)
                            .setPhotoPath(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_PATH_FAVORITE)));
                if (withBase64Photo){
                        if (personItem.getPhotoPath()!=null){
                            Bitmap personImageBitmap = BitmapFactory.decodeFile(personItem.getPhotoPath());
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            personImageBitmap.compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream);
                            byte[] imageByte = byteArrayOutputStream.toByteArray();
                            personItem.setPhoto(Base64.encodeToString(imageByte, Base64.NO_WRAP));
                        }
                    }
                return personItem;

            } else { //пользователя нет
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            closeCursor(cursor);
        }
    }


    public static boolean addNewUser(final PersonItem personItem, boolean writeToServerAlso) {
        String photoPath;
        int acccessType;
        try {
            if (personItem!=null){

                //если пользователь уже есть в базе, то удаляем старую запись
                if (isUserInBase(personItem.getRadioLabel())){
                    deleteUser(personItem.getRadioLabel());
                }

                if (personItem.getPhoto() == null) personItem.setPhoto(getBase64DefaultPhotoFromResources());

                //сохраняем фото в память
                photoPath = new ImageSaver(App.getAppContext())
                        .setFileName(personItem.getRadioLabel())
                        .save(personItem.getPhoto(), null);
                personItem.setPhotoPath(photoPath);

                if (personItem.getAccessType() == 0){
                    acccessType = CARD_USER_ACCESS;
                    personItem.setAccessType(acccessType);
                } else {
                    acccessType = personItem.getAccessType();
                }

                ContentValues cv = new ContentValues();
                cv.put(FavoriteDBinit.COLUMN_USER_ID_FAVORITE, SharedPrefs.getActiveAccountID());
                cv.put(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE, personItem.getLastname());
                cv.put(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
                cv.put(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE, personItem.getMidname());
                cv.put(FavoriteDBinit.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
                cv.put(FavoriteDBinit.COLUMN_TAG_FAVORITE, personItem.getRadioLabel());
                cv.put(FavoriteDBinit.COLUMN_SEX_FAVORITE, personItem.getSex());
                cv.put(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE, acccessType);

                if (photoPath!=null) cv.put(FavoriteDBinit.COLUMN_PHOTO_PATH_FAVORITE, photoPath);

                //пишем в базу
                DbShare.getDataBase(DbShare.DB_FAVORITE).insert(FavoriteDBinit.TABLE_PERSONS, null, cv);

                //добавляем на сервер
                if (writeToServerAlso){

                    SQL_Connection.getConnection(null, 0, new SQL_Connection.Callback() {
                        @Override
                        public void onServerConnected(Connection connection, int callingTask) {
                            new ServerWriter(ServerWriter.PERSON_UPDATE, personItem, null);
                        }

                        @Override
                        public void onServerConnectException(Exception e) {

                        }
                    });
                }

                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getPersonPhoto (String userTag, int photoLocation, int photoDimension){
        Cursor cursor = null;
        try {
            switch (photoLocation){
                case LOCAL_PHOTO:
                    cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                            FavoriteDBinit.TABLE_PERSONS,
                            new String[]{FavoriteDBinit.COLUMN_PHOTO_PATH_FAVORITE},
                            FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                            new String[]{userTag},
                            null,
                            null,
                            "1");
                    if (cursor.getCount()>0){
                        cursor.moveToFirst();
                        return cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_PATH_FAVORITE));
                    }
                    return null;
                /*case SERVER_PHOTO:
                    Connection mConnection = SQL_Connection.getConnection(null, null);
                    if (mConnection!=null){
                        try {
                            String mPhoto;
                            Statement mStatement = mConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
                            ResultSet mResult = mStatement.executeQuery("SELECT "
                                    + SQL_Connection.COLUMN_ALL_STAFF_SEX + ","
                                    + SQL_Connection.COLUMN_ALL_STAFF_PHOTO
                                    + " FROM " + SQL_Connection.ALL_STAFF_TABLE
                                    + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_TAG
                                    + " = '" + userTag + "'");
                            mResult.first();
                            if (mResult.getRow()!=0){
                                mPhoto = mResult.getString(SQL_Connection.COLUMN_ALL_STAFF_PHOTO);
                                if (mPhoto == null) mPhoto = getBase64DefaultPhotoFromResources();
                                switch (photoDimension){
                                        case FULLSIZE_PHOTO:
                                            return mPhoto;
                                        case PREVIEW_PHOTO:
                                            return getPhotoPreview(mPhoto);
                                        default:
                                            return mPhoto;
                                }
                            }
                            if (mStatement!=null) mStatement.close();
                            if (mResult!=null) mResult.close();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    return null;*/
                default:
                    return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    public static File getPersonPhotoPath(String personTag){

        Cursor cursor = null;
        if (personTag!=null){
            try {
                cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                        FavoriteDBinit.TABLE_PERSONS,
                        new String[]{FavoriteDBinit.COLUMN_PHOTO_PATH_FAVORITE},
                        FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                        new String[]{personTag},
                        null,
                        null,
                        "1");

                if (cursor.getCount()>0){
                    cursor.moveToFirst();
                    return new File(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_PHOTO_PATH_FAVORITE)));
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                closeCursor(cursor);
            }
        }
        return null;
    }

    public static void updatePersonItem(String tag, PersonItem personItem){
        Cursor cursor = null;
        try {

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
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
                if (!personItem.getLastname().equals("")){
                    cv.put(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE,personItem.getLastname());
                }
                if (!personItem.getFirstname().equals("")){
                    cv.put(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
                }
                if (!personItem.getMidname().equals("")){
                    cv.put(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE,personItem.getMidname());
                }
                if (!personItem.getDivision().equals("")){
                    cv.put(FavoriteDBinit.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
                }
                if (personItem.getAccessType() != 0 ){
                    cv.put(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE, personItem.getAccessType());
                }
                if (cv.size()!=0){
                    DbShare.getDataBase(DbShare.DB_FAVORITE).update(FavoriteDBinit.TABLE_PERSONS,
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
                    FavoriteDBinit.TABLE_PERSONS,
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

    public static ArrayList<CharacterItem> getPersonsCharacters(int accessType){

        Cursor cursor = null;
        try {
            String selection = null;
            String[] selectionArgs = null;
            if (accessType != 0){
                selection = FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE + " =?";
                selectionArgs = new String[]{String.valueOf(accessType)};
            }

            ArrayList <CharacterItem> characters = new ArrayList<>();
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
                    new String[]{FavoriteDBinit.COLUMN_LASTNAME_FAVORITE},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);
            cursor.moveToPosition(-1);
            ArrayList<String> uniqueCharacters = new ArrayList<>();
            while (cursor.moveToNext()){
                String firstSymbol = null;
                try {
                    firstSymbol = cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)).substring(0,1).toUpperCase();
                } catch (StringIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                if (firstSymbol!=null && !uniqueCharacters.contains(firstSymbol)){
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

    //получение списка радиометок (тэгов) всех пользователей
    public static ArrayList<String> getPersonsTags(){
        ArrayList <String> items = new ArrayList <>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
                    new String[]{FavoriteDBinit.COLUMN_TAG_FAVORITE},
                    null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_TAG_FAVORITE)));
            }
            return items;
        } catch (Exception e){
            e.printStackTrace();
            return items;
        } finally {
            closeCursor(cursor);
        }
    }

    //получение всех PersonItems для конкретной буквы c конкретным уровнем доступа !!! без фото !!!
    public static ArrayList<PersonItem> getPersonItems(String character, int accessType){
        ArrayList <PersonItem> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectionType = null, selectionChar = null, selection = null;
            String[] selectionArgsType = null, selectionArgsChar = null, selectionArgs = null;

            //если есть инфа о доступе
            if (accessType != 0){
                selectionType = FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE + " =?";
                selectionArgsType = new String[]{String.valueOf(accessType)};
            }

            //если есть инфа о первой букве
            if (!character.equals("#")){
                selectionChar = FavoriteDBinit.COLUMN_LASTNAME_FAVORITE + " LIKE ?";
                selectionArgsChar = new String[]{character + "%"};
            }

            //проверяем что передано в метод и на основе этого формируем запрос
            if (selectionType!=null && selectionChar!=null){
                selection = selectionType + " AND " + selectionChar;
                selectionArgs = new String[selectionArgsType.length+selectionArgsChar.length];
                System.arraycopy(selectionArgsType,0,selectionArgs,0,selectionArgsType.length);
                System.arraycopy(selectionArgsChar,0,selectionArgs,selectionArgsType.length,selectionArgsChar.length);
            } else if (selectionType!=null && selectionChar==null){
                selection = selectionType;
                selectionArgs = selectionArgsType;
            } else if (selectionType == null && selectionChar!=null){
                selection = selectionChar;
                selectionArgs = selectionArgsChar;
            }

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
                    new String[]{FavoriteDBinit.COLUMN_LASTNAME_FAVORITE,
                            FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE,
                            FavoriteDBinit.COLUMN_MIDNAME_FAVORITE,
                            FavoriteDBinit.COLUMN_DIVISION_FAVORITE,
                            FavoriteDBinit.COLUMN_TAG_FAVORITE,
                            FavoriteDBinit.COLUMN_SEX_FAVORITE,
                            FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE},
                    selection,
                    selectionArgs,
                    null,
                    FavoriteDBinit.COLUMN_LASTNAME_FAVORITE + " ASC",
                    null);

            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    items.add(new PersonItem().setLastname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)))
                            .setFirstname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE)))
                            .setMidname(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE)))
                            .setDivision(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_DIVISION_FAVORITE)))
                            .setSex(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_SEX_FAVORITE)))
                            .setRadioLabel(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_TAG_FAVORITE)))
                            .setAccessType(cursor.getInt(cursor.getColumnIndex(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE))));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static void setPersonAccessType (String tag, int accessType){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
                    new String[]{FavoriteDBinit._ID, FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE},
                    FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                    new String[]{tag},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                ContentValues cv = new ContentValues();
                cv.put(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE, accessType);
                DbShare.getDataBase(DbShare.DB_FAVORITE)
                        .update(FavoriteDBinit.TABLE_PERSONS, cv, FavoriteDBinit._ID + " = " + cursor.getInt(cursor.getColumnIndex(FavoriteDBinit._ID)), null);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public static int getPersonAccessType (String tag){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
                    new String[]{FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE},
                    FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                    new String[]{tag},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                return cursor.getInt(cursor.getColumnIndex(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return -1;
    }

    public static boolean isUserInBase(String tag){
        //SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.DB_FAVORITE);
        Cursor cursor = null;
        try {
          //  /mDataBase.compileStatement("SELECT * FROM " + FavoriteDBinit.TABLE_PERSONS
            //        + " WHERE " + FavoriteDBinit.COLUMN_TAG_FAVORITE + " = '" + tag + "'").simpleQueryForString();
            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
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

    private static String getEncodedPhoto(String personTag){
        String encoded = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(getPersonPhotoPath(personTag).getPath());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e){
            e.printStackTrace();
        }
        return encoded;
    }

    public static void backupFavoriteStaffToFile(){
        Cursor cursor = null;
        try {

            FileOutputStream fileOutputStream;
            File file = new File(SharedPrefs.getBackupLocation(), "/Persons.csv");

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE, FavoriteDBinit.TABLE_PERSONS, null,null,null,null,null,null);
            cursor.moveToPosition(-1);

            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(PERSONS_VALIDATE);
                fileOutputStream.write(stringBuilder.toString().getBytes());
                fileOutputStream.write("\n".getBytes());
                stringBuilder.delete(0, stringBuilder.length());

                String delimeter = ";";
                if (cursor.getCount()!=0){
                    while (cursor.moveToNext()){

                        String tag = cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_TAG_FAVORITE));

                        stringBuilder.append(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_LASTNAME_FAVORITE)));
                        stringBuilder.append(delimeter);
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_FIRSTNAME_FAVORITE)));
                        stringBuilder.append(delimeter);
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_MIDNAME_FAVORITE)));
                        stringBuilder.append(delimeter);
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_DIVISION_FAVORITE)));
                        stringBuilder.append(delimeter);
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_SEX_FAVORITE)));
                        stringBuilder.append(delimeter);
                        stringBuilder.append(tag);
                        stringBuilder.append(delimeter);
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex(FavoriteDBinit.COLUMN_ACCESS_TYPE_FAVORITE)));
                        stringBuilder.append(delimeter);
                        stringBuilder.append(getEncodedPhoto(tag));

                        System.out.println("string: " + stringBuilder.toString());

                        fileOutputStream.write(stringBuilder.toString().getBytes());
                        fileOutputStream.write("\n".getBytes());

                        stringBuilder.delete(0, stringBuilder.length());
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

    //удаление пользователя
    public static void deleteUser(String tag){

        Cursor cursor = null;
        try{

            //удаляем фото из хранилища
            getPersonPhotoPath(tag).delete();

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE,
                    FavoriteDBinit.TABLE_PERSONS,
                    new String[]{FavoriteDBinit._ID},
                    FavoriteDBinit.COLUMN_TAG_FAVORITE + " =?",
                    new String[]{tag},
                    null,
                    null,
                    null);

            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    DbShare.getDataBase(DbShare.DB_FAVORITE).delete(FavoriteDBinit.TABLE_PERSONS, FavoriteDBinit._ID + "=" + cursor.getInt(cursor.getColumnIndex(FavoriteDBinit._ID)), null);
                }
            }


        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }



    public static void clear(){
        try {
            File filesDir = App.getAppContext().getFilesDir();
            if (filesDir.isDirectory()){
                File[]files = filesDir.listFiles();
                for (File file : files) {
                    file.delete();
                }
            }
            DbShare.getDataBase(DbShare.DB_FAVORITE).delete(FavoriteDBinit.TABLE_PERSONS, null, null);
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

            cursor = DbShare.getCursor(DbShare.DB_FAVORITE, FavoriteDBinit.TABLE_PERSONS, new String[]{FavoriteDBinit.COLUMN_TAG_FAVORITE},null,null,null,null,null);
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

    public static String getBase64DefaultPhotoFromResources(){
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(App.getAppContext().getResources(), R.drawable.ic_user_not_found);
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
        String initials = "";
        switch (initialsType){
            case SHORT_INITIALS:
                if (lastname != null && firstname != null && midname != null && firstname.length() != 0 && midname.length() != 0) {
                    initials = lastname + " " + firstname.charAt(0) + "." + midname.charAt(0) + ".";
                } else {
                    if (lastname != null && firstname != null) {
                        initials = lastname + " " + firstname;
                    } else {
                        if (lastname != null) {
                            initials = lastname;
                        }
                    }
                }
               break;
            case FULL_INITIALS:
                if (lastname != null && firstname != null && midname != null) {
                    initials = lastname + " " + firstname + " " + midname;
                } else {
                    if (lastname != null && firstname != null) {
                        initials = lastname + " " + firstname;
                    } else {
                        if (lastname != null) {
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
