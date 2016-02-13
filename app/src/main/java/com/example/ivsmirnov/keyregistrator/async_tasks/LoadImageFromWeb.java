package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by ivsmirnov on 13.02.2016.
 */
public class LoadImageFromWeb extends AsyncTask<Void,Drawable,Drawable> {
    private String mUrl;
    private Get_Account_Information_Interface mListener;

    public LoadImageFromWeb(String url, Get_Account_Information_Interface get_account_information_interface){
        this.mUrl = url;
        this.mListener = get_account_information_interface;
    }

    @Override
    protected Drawable doInBackground(Void... params) {
        try{
            InputStream inputStream = (InputStream)new URL(mUrl).getContent();
            return Drawable.createFromStream(inputStream,"photo");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if (drawable!=null){
            mListener.onAccountImageLoaded(drawable);
        }
    }
}
