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

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class DataBaseFavorite {

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
        cursor.moveToPosition(-1);
        ArrayList <PersonItem> mPerson = new ArrayList<>();
        while (cursor.moveToNext()){
            mPerson.add(new PersonItem()
                    .setLastname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))
                    .setFirstname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_FIRSTNAME_FAVORITE)))
                    .setMidname(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_MIDNAME_FAVORITE)))
                    .setDivision(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_DIVISION_FAVORITE)))
                    .setSex(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SEX_FAVORITE)))
                    .setPhotoPreview(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_PREVIEW_FAVORITE)))
                    .setPhotoOriginal(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)))
                    .setRadioLabel(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE))));
        }
        return mPerson;
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

        options.inSampleSize = calculateInSampleSize(options, 120, 160);
        options.inJustDecodeBounds = false;

        Bitmap bitmapPrew = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapPrew.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
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
