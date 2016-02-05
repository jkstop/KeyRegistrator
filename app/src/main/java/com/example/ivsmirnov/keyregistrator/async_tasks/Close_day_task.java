package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseDayInterface;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Close_day_task extends AsyncTask<Void,Void,Void> {

    private Context mContext;
    private CloseDayInterface mListener;

    public Close_day_task(Context context, CloseDayInterface closeDayInterface){
        this.mContext = context;
        this.mListener = closeDayInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);

        dbJournal.backupJournalToXLS();
        dbFavorite.backupFavoriteStaffToFile();

        dbJournal.closeDB();
        dbFavorite.closeDB();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        try {
            new Save_to_file(mContext, Values.WRITE_JOURNAL, false).execute();
            new Save_to_file(mContext, Values.WRITE_TEACHERS, false).execute();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mListener.onClosed();
        }


    }
}
