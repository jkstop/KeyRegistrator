package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.others.App;

import java.lang.ref.WeakReference;

/**
 * Загрузка фото пользователя в указанный ImageView
 */
public class GetPersonPhoto extends AsyncTask<Void,Void,Bitmap> {

    private int mPhotoDimension, mPhotoLocation;
    private ImageView mPersonImageView;
    private String mPersonTag;
    private ProgressBar mProgressBar;

    public GetPersonPhoto (GetPersonParams getPersonParams) {
        mPhotoDimension = getPersonParams.getPersonPhotoDimension();
        mPhotoLocation = getPersonParams.getPersonPhotoLocation();
        mPersonTag = getPersonParams.getPersonTag();
        mPersonImageView = new WeakReference<>(getPersonParams.getPersonImageView()).get();
        mProgressBar = new WeakReference<>(getPersonParams.getPersonImageLoadProgressBar()).get();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mPersonImageView.setVisibility(View.INVISIBLE);
        if (mProgressBar!=null) mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        String photo = FavoriteDB.getPersonPhoto(mPersonTag, mPhotoLocation, mPhotoDimension);
        if (photo == null) photo = FavoriteDB.getBase64DefaultPhotoFromResources();
        byte [] decodedString = Base64.decode(photo, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (mProgressBar!=null) mProgressBar.setVisibility(View.INVISIBLE);
        mPersonImageView.setVisibility(View.VISIBLE);
        mPersonImageView.startAnimation(AnimationUtils.loadAnimation(App.getAppContext(), android.R.anim.fade_in));
        mPersonImageView.setImageBitmap(bitmap);
    }
}
