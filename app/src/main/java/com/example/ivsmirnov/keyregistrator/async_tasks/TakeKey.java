package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.KeyInterface;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.TakeKeyParams;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 03.02.2016.
 */
public class TakeKey extends AsyncTask<TakeKeyParams,Void,Void> {

    private Context mContext;
    private KeyInterface mListener;

    public TakeKey(Context context){
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        Values.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_take_key), Values.TOAST_POSITIVE);
    }

    @Override
    protected Void doInBackground(TakeKeyParams... params) {
        mListener = params[0].getPublicInterface();
        JournalItem journalItem = DataBaseJournal.createNewItemForJournal(mContext,
                params[0].getPersonItem(),
                params[0].getAuditroom(),
                params[0].getAccessType());
        long positionInBase = DataBaseJournal.writeInDBJournal(journalItem);
        Values.writeRoom(journalItem, params[0].getPersonItem(),positionInBase);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mListener!=null){
            mListener.onTakeKey();
        }
    }
}