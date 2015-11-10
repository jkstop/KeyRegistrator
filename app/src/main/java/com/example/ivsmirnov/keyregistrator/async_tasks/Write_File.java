package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;

public class Write_File extends AsyncTask<Void,Void,Void> {

    private Context mContext;

    public Write_File (Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);

        dbJournal.backupJournalToFile();
        dbFavorite.backupFavoriteStaffToFile();

        dbJournal.closeDB();
        dbFavorite.closeDB();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(mContext,"Запись произошла успешно",Toast.LENGTH_SHORT).show();
    }
}
