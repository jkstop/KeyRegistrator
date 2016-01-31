package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Display;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 16.11.2015.
 */
public class Save_to_file extends AsyncTask <Void,Integer,Void> {

    private Context mContext;
    private int mType;
    private Settings mSettings;
    private ProgressDialog mProgressDialog;
    private String mPathExternal;

    private static final String JOURNAL = "/Journal.xls";
    private static final String TEACHERS = "/Teachers.csv";

    public Save_to_file (Context context, int loadType){
        this.mContext = context;
        this.mType = loadType;
        mSettings = new Settings(mContext);
        mPathExternal = Environment.getExternalStorageDirectory().getPath();
        mProgressDialog = new ProgressDialog(mContext);
    }


    @Override
    protected void onPreExecute() {

        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Запись...");
        mProgressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        switch (mType){
            case Values.WRITE_JOURNAL:
                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                dbJournal.backupJournalToXLS();
                dbJournal.backupJournalToCSV();
                dbJournal.closeDB();

                String srFileJournal = mPathExternal + JOURNAL;
                String dtFileJournal = mSettings.getJournalBackupLocation() + JOURNAL;
                Values.copyfile(mContext, srFileJournal, dtFileJournal);
                break;
            case Values.WRITE_TEACHERS:
                DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
                dbFavorite.backupFavoriteStaffToFile();
                dbFavorite.closeDB();

                String srFileTeachers = mPathExternal + TEACHERS;
                String dtFileTeachers = mSettings.getPersonsBackupLocation() + TEACHERS;
                Values.copyfile(mContext, srFileTeachers, dtFileTeachers);
                break;
            case Values.WRITE_ROOMS:
                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                dbRooms.backupRoomsToFile();
                dbRooms.closeDB();
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }
}
