package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;

import java.lang.ref.WeakReference;

/**
 * Created by ivsmirnov on 19.02.2016.
 */
public class GetPersonPhoto extends AsyncTask<Void,Void,Bitmap> {

    public static final int ORIGINAL_IMAGE = 0;
    public static final int PREVIEW_IMAGE = 1;
    public static final int SERVER_PHOTO = 2;
    public static final int LOCAL_PHOTO = 3;

    private String mTag;
    private DataBaseFavorite mDataBase;
    private WeakReference<ImageView> mWeakReference;
    private int mPhotoSize;
    private int mPhotoSource;
    private ImageView mImageView;
    private Context mContext;

    public GetPersonPhoto (Context context,String radioLabel, DataBaseFavorite dataBaseFavorite, ImageView imageView, int photoSize, int photoSource){
        this.mContext = context;
        this.mTag = radioLabel;
        this.mDataBase = dataBaseFavorite;
        this.mWeakReference = new WeakReference<ImageView>(imageView);
        this.mPhotoSize = photoSize;
        this.mPhotoSource = photoSource;
        mImageView = mWeakReference.get();
    }

    @Override
    protected void onPreExecute() {
        if (mImageView!=null){
            mImageView.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (mPhotoSource == LOCAL_PHOTO){
            if (mPhotoSize == ORIGINAL_IMAGE){
                return mDataBase.getPersonPhoto(mTag, LOCAL_PHOTO, ORIGINAL_IMAGE);
            } else if (mPhotoSize == PREVIEW_IMAGE){
                return mDataBase.getPersonPhoto(mTag, LOCAL_PHOTO, PREVIEW_IMAGE);
            }else{
                return null;
            }
        } else if (mPhotoSource == SERVER_PHOTO){
            if (mPhotoSize == ORIGINAL_IMAGE){
                return mDataBase.getPersonPhoto(mTag, SERVER_PHOTO, ORIGINAL_IMAGE);
            } else if (mPhotoSize == PREVIEW_IMAGE){
                return mDataBase.getPersonPhoto(mTag, SERVER_PHOTO, PREVIEW_IMAGE);
            } else {
                return null;
            }
        }else{
            return null;
        }
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        if (mImageView!=null){

            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);

            Animation fadeInanimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            mImageView.startAnimation(fadeInanimation);

        }
    }
}
