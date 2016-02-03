package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.interfaces.KeyInterface;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.TakeKeyParams;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 03.02.2016.
 */
public class TakeKey extends AsyncTask<TakeKeyParams,Void,Void> {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private KeyInterface mListener;

    public TakeKey(Context context){
        this.mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Подождите");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(TakeKeyParams... params) {
        mListener = params[0].getPublicInterface();
        JournalItem journalItem = Values.createNewItemForJournal(mContext,
                params[0].getPersonItem(),
                params[0].getAuditroom(),
                params[0].getAccessType());
        long positionInBase = Values.writeInJournal(mContext, journalItem);
        Values.writeRoom(mContext, journalItem, params[0].getPersonItem(),positionInBase);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
       if (mProgressDialog.isShowing()){
           mProgressDialog.cancel();
       }

        if (mListener!=null){
            mListener.onTakeKey();
        }
    }
}
