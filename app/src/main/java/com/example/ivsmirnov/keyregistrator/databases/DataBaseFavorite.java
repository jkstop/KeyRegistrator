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
import android.util.SparseArray;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.async_tasks.Find_User_in_SQL_Server;
import com.example.ivsmirnov.keyregistrator.async_tasks.GetPersonPhoto;
import com.example.ivsmirnov.keyregistrator.items.CharacterItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
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
    public Cursor cursor;

    private Context mContext;
    private Settings mSettings;
   // private SharedPreferences.Editor mEditor;
   // private SharedPreferences mSharedPreferences;

    public DataBaseFavorite(Context context){
        this.mContext = context;
        dataBaseFavoriteRegist = new DataBaseFavoriteRegist(mContext);
        sqLiteDatabase = dataBaseFavoriteRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,null,null,null,null,null,null);
        mSettings = new Settings(mContext);
    }

    public PersonItem findUserByTag(String tag){
        Long start = System.currentTimeMillis();
        PersonItem personItem = new PersonItem();
        cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,new String[]{DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},null,null,null,null,null);
        cursor.moveToPosition(-1);
        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
        while (cursor.moveToNext()){
            if (tag.equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)))){
                int position = cursor.getPosition();
                cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,null,null,null,null,null,null);
                cursor.moveToPosition(position);
                personItem.setLastname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))
                        .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE)))
                        .setMidname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE)))
                        .setDivision(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE)))
                        .setSex(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE)))
                        .setPhotoPreview(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE)))
                        .setPhotoOriginal(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)))
                        .setRadioLabel(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
                break;
            }
        }
        if (personItem.isEmpty()){
            personItem = findInServer(mContext, tag);
            writeInDBTeachers(personItem);
            return personItem;
        }
        dbFavorite.closeDB();
        Long end = System.currentTimeMillis();
        Log.d("durable",String.valueOf(end - start));

        return personItem;
    }

    public PersonItem getPersonItem(String tag, int userLocation, int photoSize){
        PersonItem personItem;
        if (userLocation == LOCAL_USER){
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " =?",new String[]{tag});
            if (cursor.getCount()>0){
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
            }
        }else if (userLocation == SERVER_USER){
            Connection connection = SQL_Connector.check_sql_connection(mContext, mSettings.getServerConnectionParams());
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
                            photo = Find_User_in_SQL_Server.getBase64DefaultPhotoFromResources(mContext);
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
                }
            }else{
                Toast.makeText(mContext,"Нет подключения к серверу!",Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return null;
    }

    public ArrayList<String> getTagForCurrentCharacter(String character){
        ArrayList <String> mTags = new ArrayList<>();
        if (character.equals("Все")){
            cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, new String[]{DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},null,null,null,null,null,null);
        } else{
            cursor = sqLiteDatabase.rawQuery("SELECT " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " FROM " + DataBaseFavoriteRegist.TABLE_TEACHER +
                            " WHERE " + DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE + " LIKE?",
                    new String[]{character+"%"});
        }
        if (cursor.getCount()>0){
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                mTags.add(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
            }
        }
        return mTags;
    }

    public ArrayList<CharacterItem> getPersonsCharacters(){
        ArrayList <CharacterItem> characters = new ArrayList<>();
        cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, new String[]{DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE},null,null,null,null,null,null);
        cursor.moveToFirst();
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
    }

    public static PersonItem findInServer(Context context,String tag){
        PersonItem personItem = null;
        try {
            Connection connection = SQL_Connector.check_sql_connection(context, new Settings(context).getServerConnectionParams());
            if (connection!=null){
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from STAFF_NEW where [RADIO_LABEL] = '"+tag+"'");
                while(resultSet.next()){
                    //String namePosition = resultSet.getString("NAME_POSITION");
                    personItem = new PersonItem()
                            .setLastname(resultSet.getString("LASTNAME"))
                            .setFirstname(resultSet.getString("FIRSTNAME"))
                            .setMidname(resultSet.getString("MIDNAME"))
                            .setDivision(resultSet.getString("NAME_DIVISION"))
                            .setSex(resultSet.getString("SEX"))
                            .setPhotoPreview(getPhotoPreview(resultSet.getString("PHOTO")))
                            .setPhotoOriginal(resultSet.getString("PHOTO"))
                            .setRadioLabel(resultSet.getString("RADIO_LABEL"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return personItem;
    }

    public void closeDB(){
        dataBaseFavoriteRegist.close();
        sqLiteDatabase.close();
        cursor.close();
    }

    public boolean isAccountInBase (String tag){
        String userInBase = null;
        try {
            userInBase = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " = '" + tag + "'").simpleQueryForString();
        }catch (Exception e){
            e.printStackTrace();
        }

        return userInBase != null;
    }


  /*  public void writeCardInBase(String surname, String name, String lastname, String kaf, String tag, String gender, String photo) {
        String photoPath = savePhotoToSD(photo,surname+"_"+name+"_"+lastname);
        String originalPath = saveOriginalPhoto(photo, surname + "_" + name + "_" + lastname);
        writeInDBTeachers(surname, name, lastname, kaf, tag, gender, photoPath, photo);
    }*/

    public void writeInDBTeachers(PersonItem personItem) {
        if (personItem!=null){
            ContentValues cv = new ContentValues();
            cv.put(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE, personItem.getLastname());
            cv.put(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE, personItem.getFirstname());
            cv.put(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE, personItem.getMidname());
            cv.put(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE, personItem.getDivision());
            cv.put(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE, personItem.getRadioLabel());
            cv.put(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE, personItem.getSex());
            cv.put(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE, personItem.getPhotoPreview());
            cv.put(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE, personItem.getPhotoOriginal());
            sqLiteDatabase.insert(DataBaseFavoriteRegist.TABLE_TEACHER, null, cv);
        }
    }

    public void backupFavoriteStaffToFile(){
        File file = null;
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(mPath + "/Teachers.csv");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            itemList.add(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
        }
        try {
            if (file != null) {
                fileOutputStream = new FileOutputStream(file);
                for (int i = 0; i < itemList.size(); i++) {
                    fileOutputStream.write(itemList.get(i).getBytes());
                    fileOutputStream.write("\n".getBytes());
                }
                fileOutputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        itemList.clear();
    }

    public ArrayList<PersonItem> readTeachersFromDB(){
        cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER, new String[]{DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE,
        DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},null,null,null,null,null,null);
        cursor.moveToPosition(-1);
        ArrayList <PersonItem> mPerson = new ArrayList<>();
        while (cursor.moveToNext()){
            mPerson.add(new PersonItem()
                    .setLastname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))
                    //.setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE)))
                    //.setMidname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE)))
                    //.setDivision(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE)))
                    //.setSex(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE)))
                    /*.setPhotoPreview(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE)))
                    .setPhotoOriginal(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)))*/
                    .setRadioLabel(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE))));
        }
        Collections.sort(mPerson, new Comparator<PersonItem>() {
            @Override
            public int compare(PersonItem lhs, PersonItem rhs) {
                return lhs.getLastname().compareToIgnoreCase(rhs.getLastname());
            }
        });
        return mPerson;
    }

    public int getPersonsCount(){
        return cursor.getCount();
    }

    public PersonItem readTeacherItem(int position){
        cursor.moveToPosition(position);
        return new PersonItem()
                    .setLastname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))
                    .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE)))
                    .setMidname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE)))
                    .setDivision(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE)))
                    .setSex(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE)))
                    .setPhotoPreview(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE)))
                    .setPhotoOriginal(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)))
                    .setRadioLabel(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
    }

    public Bitmap getPersonPhoto(String tag, int photoSource, int photoSize){

        if (photoSource == GetPersonPhoto.LOCAL_PHOTO){
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " =?",new String[]{tag});
            cursor.moveToFirst();
            String photo = null;
            if (photoSize == GetPersonPhoto.ORIGINAL_IMAGE){
                photo = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE));
            }else if (photoSize == GetPersonPhoto.PREVIEW_IMAGE){
                photo = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE));
            }
            if (photo!=null){
                byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }else{
                return null;
            }

        }else if (photoSource == GetPersonPhoto.SERVER_PHOTO){
            Connection connection = SQL_Connector.check_sql_connection(mContext, mSettings.getServerConnectionParams());
            if (connection!=null){
                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select * from STAFF_NEW where [RADIO_LABEL] = '" + tag + "'");
                    while (resultSet.next()){
                        String photo = resultSet.getString("PHOTO");
                        if (photo == null){
                            photo = Find_User_in_SQL_Server.getBase64DefaultPhotoFromResources(mContext);
                        }
                        if (photoSize == GetPersonPhoto.PREVIEW_IMAGE){
                            photo = getPhotoPreview(photo);
                        }
                        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(mContext,"Нет подключения к серверу!",Toast.LENGTH_SHORT).show();
            }
            return null;
        }else{
            return null;
        }
    }



    public String getPersonPhotoBase64 (String tag, int photoSize, int photoSource){
        if (photoSource == GetPersonPhoto.LOCAL_PHOTO){
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " =?",new String[]{tag});
            cursor.moveToFirst();
            if (photoSize == GetPersonPhoto.ORIGINAL_IMAGE){
                return cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE));
            }else if (photoSize == GetPersonPhoto.PREVIEW_IMAGE){
                return cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE));
            }else{
                return null;
            }
        }else if (photoSource == GetPersonPhoto.SERVER_PHOTO){
            Connection connection = SQL_Connector.check_sql_connection(mContext, mSettings.getServerConnectionParams());
            if (connection!=null){
                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select * from STAFF_NEW where [RADIO_LABEL] = '" + tag + "'");
                    while (resultSet.next()){
                        String photo = resultSet.getString("PHOTO");
                        if (photo == null){
                            photo = Find_User_in_SQL_Server.getBase64DefaultPhotoFromResources(mContext);
                        }
                        if (photoSize == GetPersonPhoto.PREVIEW_IMAGE){
                            photo = getPhotoPreview(photo);
                        }
                        return photo;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(mContext,"Нет подключения к серверу!",Toast.LENGTH_SHORT).show();
            }
            return null;
        }else{
            return null;
        }
    }




    public void deleteuser(String tag){
        cursor.moveToPosition(-1);

        cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                    + " WHERE " + DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE + " =?",new String[]{tag});
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            while (cursor.moveToNext()){
                sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, DataBaseFavoriteRegist._ID + "=" + cursor.getPosition(), null);
                Log.d("deleted", String.valueOf(cursor.getPosition()));
            }
        }




    }


    public void deleteFromTeachersDB(PersonItem personItem){
        String personPosition = sqLiteDatabase.compileStatement("SELECT * FROM " + DataBaseFavoriteRegist.TABLE_TEACHER
                + " WHERE " + DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE + " = '" + personItem.getLastname() + "'"
                + " AND " + DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE + " = '" + personItem.getFirstname() + "'"
                + " AND " + DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE + " = '" + personItem.getMidname() + "'"
                + " AND " + DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE + " = '" + personItem.getDivision() + "'" ).simpleQueryForString();
        if (!personPosition.isEmpty()){
            sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, DataBaseFavoriteRegist._ID + "=" + personPosition, null);
        }
    }

    public void clearTeachersDB(){
        sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, null, null);
    }

    public void updateTeachersDB(ArrayList<String> source, ArrayList<String> edited){
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (source.get(Values.DIALOG_PERSON_INFORMATION_KEY_LASTNAME).equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    source.get(Values.DIALOG_PERSON_INFORMATION_KEY_FIRSTNAME).equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE)))&&
                    source.get(Values.DIALOG_PERSON_INFORMATION_KEY_MIDNAME).equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE)))&&
                    source.get(Values.DIALOG_PERSON_INFORMATION_KEY_DIVISION).equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE)))){
                int row = cursor.getInt(cursor.getColumnIndex(DataBaseFavoriteRegist._ID));
                ContentValues cv = new ContentValues();
                cv.put(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE,edited.get(Values.DIALOG_PERSON_INFORMATION_KEY_LASTNAME));
                cv.put(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE,edited.get(Values.DIALOG_PERSON_INFORMATION_KEY_FIRSTNAME));
                cv.put(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE,edited.get(Values.DIALOG_PERSON_INFORMATION_KEY_MIDNAME));
                cv.put(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE,edited.get(Values.DIALOG_PERSON_INFORMATION_KEY_DIVISION));
                sqLiteDatabase.update(DataBaseFavoriteRegist.TABLE_TEACHER, cv, DataBaseFavoriteRegist._ID + "=" + row, null);
            }
        }
    }

    public static String getPhotoPreview(String photo){

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
    }



    public static int calculateInSampleSize(
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
    }
}
