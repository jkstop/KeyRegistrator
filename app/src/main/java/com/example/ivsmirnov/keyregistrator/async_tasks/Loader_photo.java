package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_Image;

/**
 * Created by IVSmirnov on 28.07.2015.
 */
public class Loader_photo extends AsyncTask <String,Void,Bitmap> {

    private  Context context;
    private String item;
    private Loader_Image.OnImageLoaded listener;

    public Loader_photo(Context c, String i,Loader_Image.OnImageLoaded l){
        this.context = c;
        this.item = i;
        this.listener = l;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        byte[] decodedString = Base64.decode(item, Base64.DEFAULT);
        Bitmap photo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return photo;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        //listener.doneLoad(true);
    }
}
