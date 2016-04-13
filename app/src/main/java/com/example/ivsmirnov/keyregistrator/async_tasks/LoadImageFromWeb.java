package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Загрузка изображения из интернета
 */
public class LoadImageFromWeb extends AsyncTask<Void,Bitmap,Bitmap> {
    private String mUrl;
    private GetAccountInterface mGetAccountInterface;

    public LoadImageFromWeb(String url, GetAccountInterface getAccountInterface){
        this.mUrl = url;
        this.mGetAccountInterface = getAccountInterface;
        System.out.println("load image from web *********************************");
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try{
            URL imageUrl = new URL(mUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)imageUrl.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            InputStream inputStream = httpURLConnection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        System.out.println("load image from web -------------------------------------");
        if (bitmap!=null){
            mGetAccountInterface.onAccountImageLoaded(bitmap);
        }
    }
}
