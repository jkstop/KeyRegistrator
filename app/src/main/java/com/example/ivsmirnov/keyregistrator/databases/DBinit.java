package com.example.ivsmirnov.keyregistrator.databases;

import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.interfaces.DBinterface;

/**
 * Created by ivsmirnov on 10.03.2016.
 */
public class DBinit extends AsyncTask<Context, Void, Void> {

    private DataBaseFavorite DataBaseFavorite;
    private DataBaseJournal DataBaseJournal;
    private DataBaseRooms DataBaseRooms;
    private DataBaseAccount DataBaseAccount;

    private DBinterface mListener;

    public DBinit(DBinterface dBinterface){
        this.mListener = dBinterface;
    }

    public DataBaseFavorite getDataBaseFavorite(){
        return DataBaseFavorite;
    }

    public DataBaseJournal getDataBaseJournal(){
        return DataBaseJournal;
    }

    public DataBaseRooms getDataBaseRooms(){
        return DataBaseRooms;
    }

    public DataBaseAccount getDataBaseAccount(){
        return DataBaseAccount;
    }

    public boolean isDBinited(){
        if (DataBaseFavorite != null &&
                DataBaseJournal !=null &&
                DataBaseAccount !=null &&
                DataBaseRooms != null){
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Context... params) {

        if (DataBaseFavorite == null) DataBaseFavorite = new DataBaseFavorite(params[0]);
        if (DataBaseJournal == null) DataBaseJournal = new DataBaseJournal(params[0]);
        if (DataBaseRooms == null) DataBaseRooms = new DataBaseRooms(params[0]);
        if (DataBaseAccount == null) DataBaseAccount = new DataBaseAccount(params[0]);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (DataBaseFavorite != null &&
                DataBaseJournal !=null &&
                DataBaseAccount !=null &&
                DataBaseRooms != null){
            mListener.onDBinited(true);
        } else {
            mListener.onDBinited(false);
        }
    }
}
