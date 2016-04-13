package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.items.GetJournalParams;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;

import java.lang.ref.WeakReference;
import java.sql.Time;

/**
 * Загрузка журнала
 */
public class GetJournal extends AsyncTask<Void,Void,JournalItem> {

    private CardView mCard;
    private Animation mAnimation;
    private TextView mTextAuditroom;
    private TextView mTextTimeIn;
    private TextView mTextTimeOut;
    private ImageView mImagePerson;
    private TextView mTextInitials;
    private ImageView mImageAccess;
    private long mTimeIn;

    public GetJournal (GetJournalParams journalParams, Animation animation){
        this.mCard = new WeakReference<>(journalParams.getCard()).get();
        this.mTextAuditroom = new WeakReference<>(journalParams.getTextAuditroom()).get();
        this.mTextTimeIn = new WeakReference<>(journalParams.getTextTimeIn()).get();
        this.mTextTimeOut = new WeakReference<>(journalParams.getTextTimeOut()).get();
        this.mImagePerson = new WeakReference<>(journalParams.getImagePerson()).get();
        this.mTextInitials = new WeakReference<>(journalParams.getTextInitials()).get();
        this.mImageAccess = new WeakReference<>(journalParams.getImageAccess()).get();
        this.mTimeIn = journalParams.getTimeIn();
        this.mAnimation = animation;
    }

    @Override
    protected void onPreExecute() {
        System.out.println("get journal ***************************************");
        mCard.setVisibility(View.INVISIBLE);
    }

    @Override
    protected JournalItem doInBackground(Void... params) {

        return JournalDB.getJournalItem(mTimeIn);
    }

    @Override
    protected void onPostExecute(JournalItem journalItem) {
        System.out.println("get journal ---------------------------------");
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

        if (mTextInitials!=null) mTextInitials.setText(FavoriteDB
                .getPersonInitials(FavoriteDB.FULL_INITIALS, journalItem.getPersonLastname(), journalItem.getPersonFirstname(), journalItem.getPersonMidname()));


        if (mImagePerson!=null){
            if (journalItem.getPersonPhoto()!=null){
                byte[] decodedString = Base64.decode(journalItem.getPersonPhoto(), Base64.DEFAULT);
                mImagePerson.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            }
        }

        if (mImageAccess!=null){
            if (journalItem.getAccessType() == FavoriteDB.CARD_USER_ACCESS){
                mImageAccess.setImageResource(R.drawable.ic_credit_card_black_18dp);
            } else {
                mImageAccess.setImageResource(R.drawable.ic_touch_app_black_24dp);
            }
        }

        mCard.setVisibility(View.VISIBLE);
        mCard.startAnimation(mAnimation);
    }
}
