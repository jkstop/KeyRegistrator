package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 16.11.2015.
 */
public class Save_to_file extends AsyncTask <Void,Integer,Void> {

    private Context mContext;
    private int mType;
    private SharedPreferences mSharedPreferences;
    private ProgressDialog mProgressDialog;
    private String mPathExternal;
    private String mPathForCopy;

    private static final String JOURNAL = "/Journal.txt";
    private static final String TEACHERS = "/Teachers.csv";

    public Save_to_file (Context context, int loadType){
        this.mContext = context;
        this.mType = loadType;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPathExternal = Environment.getExternalStorageDirectory().getPath();
        mProgressDialog = new ProgressDialog(mContext);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Запись в файл...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        switch (mType){
            case Values.WRITE_JOURNAL:
                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                dbJournal.backupJournalToFile();
                dbJournal.closeDB();

                mPathForCopy = mSharedPreferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                String srFileJournal = mPathExternal + JOURNAL;
                String dtFileJournal = mPathForCopy + JOURNAL;
                Values.copyfile(mContext, srFileJournal, dtFileJournal);
                break;
            case Values.WRITE_TEACHERS:
                DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
                dbFavorite.backupFavoriteStaffToFile();
                dbFavorite.closeDB();

                mPathForCopy = mSharedPreferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_TEACHERS,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                String srFileTeachers = mPathExternal + TEACHERS;
                String dtFileTeachers = mPathForCopy + TEACHERS;
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
        Toast.makeText(mContext,"Готово!",Toast.LENGTH_SHORT).show();
    }
}
