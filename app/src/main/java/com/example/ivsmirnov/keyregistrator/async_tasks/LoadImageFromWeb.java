package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by ivsmirnov on 13.02.2016.
 */
public class LoadImageFromWeb extends AsyncTask<Void,Bitmap,Bitmap> {
    private String mUrl;
    private GetAccountInterface mGetAccountInterface;

    public LoadImageFromWeb(String url, GetAccountInterface getAccountInterface){
        this.mUrl = url;
        this.mGetAccountInterface = getAccountInterface;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try{
            InputStream inputStream = (InputStream)new URL(mUrl).getContent();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap!=null){
            mGetAccountInterface.onAccountImageLoaded(bitmap);
        }
    }
}
