package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Сохранение фото пользователей во внутреннее хранилище
 */
public class ImageSaver {

    private Context mContext;
    private String mFileName;

    public ImageSaver (Context context) {
        mContext = context;
    }

    public ImageSaver setFileName (String fileName){
        mFileName = fileName + ".webp";
        return this;
    }

    public String save (String photo){
        FileOutputStream fileOutputStream = null;
        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        Bitmap bitmap;
        try {
            File file = createFile();
            fileOutputStream = new FileOutputStream(file);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
            return file.getAbsolutePath();

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream!=null) fileOutputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public Bitmap load (){
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(createFile());
            return BitmapFactory.decodeStream(fileInputStream);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream!=null) fileInputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public File getImage (){
        return createFile();
    }

    private File createFile(){
        return new File(mContext.getFilesDir(), mFileName);
    }

}
