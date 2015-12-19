package com.example.ivsmirnov.keyregistrator.databases;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 05.11.2015.
 */
public class DataBaseFavorite {

    public DataBaseFavoriteRegist dataBaseFavoriteRegist;
    public SQLiteDatabase sqLiteDatabase;
    public Cursor cursor;

    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mSharedPreferences;

    public DataBaseFavorite(Context context){
        this.mContext = context;
        dataBaseFavoriteRegist = new DataBaseFavoriteRegist(mContext);
        sqLiteDatabase = dataBaseFavoriteRegist.getWritableDatabase();
        cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,null,null,null,null,null,null);
        mEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SparseArray<String> findUserByTag(String tag){
        SparseArray<String> items = new SparseArray<>();
        cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,new String[]{DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE},null,null,null,null,null);
        cursor.moveToPosition(-1);
        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
        while (cursor.moveToNext()){
            if (tag.equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)))){
                int position = cursor.getPosition();
                cursor = sqLiteDatabase.query(DataBaseFavoriteRegist.TABLE_TEACHER,null,null,null,null,null,null);
                cursor.moveToPosition(position);
                items.put(0, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE)));
                items.put(1,"none");
                items.put(2,cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)));
                items.put(3,cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)));
                items.put(4,cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)));
                items.put(5, "none");
                items.put(6, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_FAVORITE)));
                items.put(7, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
                break;
            }
        }
        if (items.size()==0){
            //DataBaseStaff dbStaff = new DataBaseStaff(mContext);
            //items = dbStaff.findUserByTag(tag);
            //dbStaff.closeDB();
            String ip = mSharedPreferences.getString(Values.SQL_SERVER,"");
            String classs = "net.sourceforge.jtds.jdbc.Driver";
            String db = "KeyRegistratorBase";
            String user = mSharedPreferences.getString(Values.SQL_USER,"");
            String password = mSharedPreferences.getString(Values.SQL_PASSWORD,"");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Connection conn = null;
            String ConnURL = null;
            try {
                Class.forName(classs);
                ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                        + "database=" + db + ";user=" + user + ";password="
                        + password + ";";
                conn = DriverManager.getConnection(ConnURL);
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from STAFF where [RADIO_LABEL] = '"+tag+"'");
                while (resultSet.next()){

                    String nameDivision = resultSet.getString("NAME_DIVISION");
                    String namePosition = resultSet.getString("NAME_POSITION");
                    String lastname = resultSet.getString("LASTNAME");
                    String firstname = resultSet.getString("FIRSTNAME");
                    String midname = resultSet.getString("MIDNAME");
                    String sex = resultSet.getString("SEX");
                    String photo = saveOriginalPhoto(resultSet.getString("PHOTO"),resultSet.getString("LASTNAME")+"_"+resultSet.getString("FIRSTNAME")+"_"+resultSet.getString("MIDNAME"));
                    String radioLabel = resultSet.getString("RADIO_LABEL");

                    items.put(0,nameDivision);
                    items.put(1,namePosition);
                    items.put(2,lastname);
                    items.put(3,firstname);
                    items.put(4,midname);
                    items.put(5,sex);
                    items.put(6,photo);
                    items.put(7,radioLabel);

                    writeInDBTeachers(lastname,firstname,midname,nameDivision,radioLabel,sex,photo,photo);
                }
                if (items.size()==0){
                    String originalPath = dbFavorite.saveOriginalPhoto("null", tag);
                    items.put(0, "-");
                    items.put(1,"-");
                    items.put(2,"Аноним");
                    items.put(3,"Аноним");
                    items.put(4,"Аноним");
                    items.put(5, "-");
                    items.put(6, originalPath);
                    items.put(7, tag);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(mContext,"Нет подключения к серверу",Toast.LENGTH_SHORT).show();
            }
        }
        dbFavorite.closeDB();

        return items;
    }

    public void closeDB(){
        dataBaseFavoriteRegist.close();
        sqLiteDatabase.close();
        cursor.close();
    }

    public String findPhotoPath(String[] items){
        String path = "";
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (items[0].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)))&&
                    items[1].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)))&&
                    items[2].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    items[3].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE)))){
                path = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_FAVORITE));
            }
        }
        return path;
    }

    public void writeCardInBase(String surname, String name, String lastname, String kaf, String tag, String gender, String photo) {
        String photoPath = savePhotoToSD(photo,surname+"_"+name+"_"+lastname);
        String originalPath = saveOriginalPhoto(photo, surname + "_" + name + "_" + lastname);
        writeInDBTeachers(surname, name, lastname, kaf, tag, gender, photoPath, originalPath);
    }

    public void writeInDBTeachers(String surname, String name, String lastname, String kaf, String tag, String gender, String photo, String originalPhoto) {
        ContentValues cv = new ContentValues();
        cv.put(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE, surname);
        cv.put(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE, name);
        cv.put(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE,lastname);
        cv.put(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE, kaf);
        cv.put(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE,tag);
        cv.put(DataBaseFavoriteRegist.COLUMN_GENDER_FAVORITE, gender);
        cv.put(DataBaseFavoriteRegist.COLUMN_PHOTO_FAVORITE, photo);
        cv.put(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE, originalPhoto);
        long id = sqLiteDatabase.insert(DataBaseFavoriteRegist.TABLE_TEACHER, null, cv);
        mEditor.putLong("id",id);
        mEditor.apply();
        Log.d("Write in Teachers DB", "OK");
    }

    public String findTagUser(String[]FIO){
        String tag = "null";
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (FIO[0].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)))&&
                    FIO[1].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)))&&
                    FIO[2].equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))){
                tag = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE));
            }
        }
        return tag;
    }

    public void backupFavoriteStaffToFile(){
        File file = null;
        ArrayList <String> itemList = new ArrayList<>();
        FileOutputStream fileOutputStream;
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(mPath + "/Teachers.csv");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            File f = new File(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)));
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);

            itemList.add(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE))+";"
                    +cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_GENDER_FAVORITE))+";"
                    + encoded);
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

    public ArrayList<SparseArray> readTeachersFromDB(){
        cursor.moveToPosition(-1);
        ArrayList <SparseArray> items = new ArrayList<>();
        while (cursor.moveToNext()){
            SparseArray<String> card = new SparseArray<>();
            card.put(0, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)));
            card.put(1, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)));
            card.put(2, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)));
            card.put(3, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE)));
            card.put(4, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_GENDER_FAVORITE)));
            card.put(5, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_FAVORITE)));
            card.put(6, cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_ORIGINAL_FAVORITE)));
            card.put(7,cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_TAG_FAVORITE)));
            items.add(card);
        }
        return items;
    }


    public void deleteFromTeachersDB(String surname, String name,String lastname,String kaf){
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (surname.equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)))&&
                    name.equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)))&&
                    lastname.equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    kaf.equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE)))){
                int row = cursor.getInt(cursor.getColumnIndex(DataBaseFavoriteRegist._ID));
                sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER,DataBaseFavoriteRegist._ID + "=" + row,null);
            }
        }
    }

    public void clearTeachersDB(){
        sqLiteDatabase.delete(DataBaseFavoriteRegist.TABLE_TEACHER, null, null);
        Log.d("Clear Teachers DB", "OK");
    }

    public String getPhotoID(SparseArray<String> items){
        String photoID = "null";
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (items.get(0).equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)))&&
                    items.get(1).equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)))&&
                    items.get(2).equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    items.get(3).equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE)))){
                photoID = cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_PHOTO_FAVORITE));
            }
        }
        return photoID;
    }

    public void updateTeachersDB(String [] source, String [] edited){

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            if (source[0].equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE)))&&
                    source[1].equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE)))&&
                    source[2].equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE)))&&
                    source[3].equals(cursor.getString(cursor.getColumnIndex(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE)))){
                int row = cursor.getInt(cursor.getColumnIndex(DataBaseFavoriteRegist._ID));
                ContentValues cv = new ContentValues();
                cv.put(DataBaseFavoriteRegist.COLUMN_SURNAME_FAVORITE,edited[0]);
                cv.put(DataBaseFavoriteRegist.COLUMN_NAME_FAVORITE,edited[1]);
                cv.put(DataBaseFavoriteRegist.COLUMN_LASTNAME_FAVORITE,edited[2]);
                cv.put(DataBaseFavoriteRegist.COLUMN_KAF_FAVORITE,edited[3]);
                sqLiteDatabase.update(DataBaseFavoriteRegist.TABLE_TEACHER, cv, DataBaseFavoriteRegist._ID + "=" + row, null);
            }
        }
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
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_female_colored);
            }else{
                String[] splitted = filename.split("_");
                if (splitted[0].substring(splitted[0].length() - 1).equalsIgnoreCase("а")) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_female_colored);
                } else {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_male_colored);
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

    public String saveOriginalPhoto(String photo, String filename) {
        Log.d("photo",photo);
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
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_female_colored);
            } else {
                String[] splitted = filename.split("_");
                if (splitted[0].substring(splitted[0].length() - 1).equalsIgnoreCase("а")) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_female_colored);
                } else {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_male_colored);
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
