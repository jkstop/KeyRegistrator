package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import java.lang.ref.WeakReference;

/**
 * Created by ivsmirnov on 21.02.2016.
 */
public class GetPersons extends AsyncTask<GetPersonParams,Void,PersonItem>{

    private WeakReference<ImageView> mWeakPersonImage;
    private WeakReference<TextView> mWeakPersonLastname, mWeakPersonFirstname, mWeakPersonMidname, mWeakPersonDivision;
    private WeakReference<CardView> mWeakPersonCard;
    private int userLocation, photoDimension;
    private CardView personCard;
    private Animation mAnimation;

    public GetPersons(CardView personCard, Animation fadeInAnimation){
        mWeakPersonCard = new WeakReference<CardView>(personCard);
        this.personCard = mWeakPersonCard.get();
        this.mAnimation = fadeInAnimation;
    }

    @Override
    protected void onPreExecute() {
        personCard.setVisibility(View.INVISIBLE);
    }

    @Override
    protected PersonItem doInBackground(GetPersonParams... params) {
        userLocation = params[0].getPersonLocation();
        photoDimension = params[0].getPersonPhotoDimension();
        DataBaseFavorite dataBaseFavorite = params[0].getDataBaseFavorite();
        PersonItem personItem = dataBaseFavorite.getPersonItem(params[0].getPersonTag(), userLocation, photoDimension);
        this.mWeakPersonImage = new WeakReference<ImageView>(params[0].getPersonImageView());
        this.mWeakPersonLastname = new WeakReference<TextView>(params[0].getPersonLastname());
        this.mWeakPersonFirstname = new WeakReference<TextView>(params[0].getPersonFirstname());
        this.mWeakPersonMidname = new WeakReference<TextView>(params[0].getPersonMidname());
        this.mWeakPersonDivision = new WeakReference<TextView>(params[0].getPersonDivision());
        return personItem;
    }

    @Override
    protected void onPostExecute(PersonItem personItem) {
        ImageView personImage = mWeakPersonImage.get();
        if (personImage!=null){
            byte[] decodedString;
            if (photoDimension == DataBaseFavorite.FULLSIZE_PHOTO){
                decodedString = Base64.decode(personItem.getPhotoOriginal(), Base64.DEFAULT);
            }else{
                decodedString = Base64.decode(personItem.getPhotoPreview(), Base64.DEFAULT);
            }
            personImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        }

        TextView textLastname = mWeakPersonLastname.get();
        if (textLastname!=null){
            textLastname.setText(personItem.getLastname());
        }

        TextView textFirstname = mWeakPersonFirstname.get();
        if (textFirstname!=null){
            textFirstname.setText(personItem.getFirstname());
        }

        TextView textMidname = mWeakPersonMidname.get();
        if (textMidname!=null){
            textMidname.setText(personItem.getMidname());
        }

        TextView textDivision = mWeakPersonDivision.get();
        if (textDivision!=null){
            textDivision.setText(personItem.getDivision());
        }
        personCard.setVisibility(View.VISIBLE);
        personCard.startAnimation(mAnimation);
    }

}
