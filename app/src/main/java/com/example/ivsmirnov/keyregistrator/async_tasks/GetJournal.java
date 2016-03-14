package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.items.GetJournalParams;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.lang.ref.WeakReference;
import java.sql.Time;

/**
 * Created by ivsmirnov on 25.02.2016.
 */
public class GetJournal extends AsyncTask<Void,Void,JournalItem> {

    private CardView mCard;
    private Animation mAnimation;
    private TextView mTextAuditroom;
    private TextView mTextTimeIn;
    private TextView mTextTimeOut;
    private ImageView mImagePerson;
    private TextView mTextLastname;
    private TextView mTextFirstname;
    private TextView mTextMidname;
    private ImageView mImageAccess;
    private long mTimeIn;

    public GetJournal (GetJournalParams journalParams, Animation animation){
        this.mCard = new WeakReference<CardView>(journalParams.getCard()).get();
        this.mTextAuditroom = new WeakReference<TextView>(journalParams.getTextAuditroom()).get();
        this.mTextTimeIn = new WeakReference<TextView>(journalParams.getTextTimeIn()).get();
        this.mTextTimeOut = new WeakReference<TextView>(journalParams.getTextTimeOut()).get();
        this.mImagePerson = new WeakReference<ImageView>(journalParams.getImagePerson()).get();
        this.mTextLastname = new WeakReference<TextView>(journalParams.getTextLastname()).get();
        this.mTextFirstname = new WeakReference<TextView>(journalParams.getTextFirstname()).get();
        this.mTextMidname = new WeakReference<TextView>(journalParams.getTextMidname()).get();
        this.mImageAccess = new WeakReference<ImageView>(journalParams.getImageAccess()).get();
        this.mTimeIn = journalParams.getTimeIn();
        this.mAnimation = animation;
    }

    @Override
    protected void onPreExecute() {
        mCard.setVisibility(View.INVISIBLE);
    }

    @Override
    protected JournalItem doInBackground(Void... params) {

        return DataBaseJournal.getJournalItem(mTimeIn);
    }

    @Override
    protected void onPostExecute(JournalItem journalItem) {
        if (mTextAuditroom!=null){
            mTextAuditroom.setText(journalItem.getAuditroom());
        }

        if (mTextTimeIn!=null){
            mTextTimeIn.setText(String.valueOf(new Time(journalItem.getTimeIn())));
        }

        if (mTextTimeOut!=null){
            if (journalItem.getTimeOut() == 0){
                mTextTimeOut.setText(R.string.journal_card_during_lesson);
            } else {
                mTextTimeOut.setText(String.valueOf(new Time(journalItem.getTimeOut())));
            }
        }

        if (mTextLastname!=null){
            mTextLastname.setText(journalItem.getPersonLastname());
        }

        if (mTextFirstname!=null){
            mTextFirstname.setText(journalItem.getPersonFirstname());
        }

        if (mTextMidname!=null){
            mTextMidname.setText(journalItem.getPersonMidname());
        }

        if (mImagePerson!=null){
            if (journalItem.getPersonPhoto()!=null){
                byte[] decodedString = Base64.decode(journalItem.getPersonPhoto(), Base64.DEFAULT);
                mImagePerson.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            }
        }

        if (mImageAccess!=null){
            if (journalItem.getAccessType() == DataBaseJournal.ACCESS_BY_CARD){
                mImageAccess.setImageResource(R.drawable.ic_credit_card_black_18dp);
            } else {
                mImageAccess.setImageResource(R.drawable.ic_click_icon);
            }
        }

        mCard.setVisibility(View.VISIBLE);
        mCard.startAnimation(mAnimation);
    }
}
