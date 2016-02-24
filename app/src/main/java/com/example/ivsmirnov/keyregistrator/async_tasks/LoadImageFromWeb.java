package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by ivsmirnov on 13.02.2016.
 */
public class LoadImageFromWeb extends AsyncTask<Void,Bitmap,Bitmap> {
    private String mUrl;
    private Get_Account_Information_Interface mListener;

    public LoadImageFromWeb(String url, Get_Account_Information_Interface get_account_information_interface){
        this.mUrl = url;
        this.mListener = get_account_information_interface;
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
            mListener.onAccountImageLoaded(bitmap);
        }
    }
}
