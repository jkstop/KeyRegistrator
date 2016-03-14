package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
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
    private ProgressDialog mProgressDialog;
    private String mPathExternal;
    private boolean isShowDialog;


    private static final String JOURNAL = "/Journal.xls";
    private static final String TEACHERS = "/Teachers.csv";

    public Save_to_file (Context context, int loadType, boolean isShowDialog){
        this.mContext = context;
        this.mType = loadType;
        this.isShowDialog = isShowDialog;
        mPathExternal = Environment.getExternalStorageDirectory().getPath();
        mProgressDialog = new ProgressDialog(mContext);


    }

    @Override
    protected void onPreExecute() {
        if (isShowDialog){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Запись...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        switch (mType){
            case Values.WRITE_JOURNAL:

                DataBaseJournal.backupJournalToXLS();
                DataBaseJournal.backupJournalToCSV();

                String srFileJournal = mPathExternal + JOURNAL;
                String dtFileJournal = Settings.getJournalBackupLocation() + JOURNAL;
                Values.copyfile(srFileJournal, dtFileJournal);
                break;
            case Values.WRITE_TEACHERS:

                DataBaseFavorite.backupFavoriteStaffToFile();

                String srFileTeachers = mPathExternal + TEACHERS;
                String dtFileTeachers = Settings.getPersonsBackupLocation() + TEACHERS;
                Values.copyfile(srFileTeachers, dtFileTeachers);
                break;
            case Values.WRITE_ROOMS:
                DataBaseRooms.backupRoomsToFile();
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }
}
