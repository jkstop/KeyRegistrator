package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.items.GetPersonParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.lang.ref.WeakReference;

/**
 * Created by ivsmirnov on 21.02.2016.
 */
public class GetPersons extends AsyncTask<GetPersonParams,Void,PersonItem>{

    private int  photoDimension;
    private CardView personCard;
    private Animation mAnimation;
    private boolean isFreeUser;

    private ImageView mPersonImage, mAccessImage;
    private TextView mPersonLastname, mPersonFirstname, mPersonMidname, mPersonDivision;

    public GetPersons(CardView personCard, Animation fadeInAnimation){
        this.personCard = new WeakReference<CardView>(personCard).get();
        this.mAnimation = fadeInAnimation;
    }

    @Override
    protected void onPreExecute() {
        personCard.setVisibility(View.INVISIBLE);
    }

    @Override
    protected PersonItem doInBackground(GetPersonParams... params) {

        photoDimension = params[0].getPersonPhotoDimension();
        DataBaseFavorite dataBaseFavorite = params[0].getDataBaseFavorite();
        PersonItem personItem = dataBaseFavorite.getPersonItem(params[0].getPersonTag(), params[0].getPersonLocation(), photoDimension);

        mPersonImage = new WeakReference<ImageView>(params[0].getPersonImageView()).get();
        mAccessImage = new WeakReference<ImageView>(params[0].getAccessImageView()).get();
        mPersonLastname = new WeakReference<TextView>(params[0].getPersonLastname()).get();
        mPersonFirstname = new WeakReference<TextView>(params[0].getPersonFirstname()).get();
        mPersonMidname = new WeakReference<TextView>(params[0].getPersonMidname()).get();
        mPersonDivision = new WeakReference<TextView>(params[0].getPersonDivision()).get();

        isFreeUser = params[0].getFreeUser();

        return personItem;
    }

    @Override
    protected void onPostExecute(PersonItem personItem) {

        if (mPersonImage!=null){
            byte[] decodedString;
            if (photoDimension == DataBaseFavorite.FULLSIZE_PHOTO){
                decodedString = Base64.decode(personItem.getPhotoOriginal(), Base64.DEFAULT);
            }else{
                decodedString = Base64.decode(personItem.getPhotoPreview(), Base64.DEFAULT);
            }
            mPersonImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        }

        if (mAccessImage!=null)
            if (isFreeUser) mAccessImage.setImageResource(R.drawable.ic_touch_app_black_24dp);
            else  mAccessImage.setImageResource(R.drawable.ic_credit_card_black_24dp);

        if (mPersonLastname!=null) mPersonLastname.setText(personItem.getLastname());

        if (mPersonFirstname!=null) mPersonFirstname.setText(personItem.getFirstname());

        if (mPersonMidname!=null) mPersonMidname.setText(personItem.getMidname());

        if (mPersonDivision!=null) mPersonDivision.setText(personItem.getDivision());


        personCard.setVisibility(View.VISIBLE);
        personCard.startAnimation(mAnimation);
    }

}
