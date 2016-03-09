package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;

import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ivsmirnov on 10.12.2015.
 */
public class Load_from_server extends AsyncTask<Void,Void,Void> {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;
    private Settings mSettings;
    private DataBaseJournal mDataBaseJournal;

    public Load_from_server(Context context, UpdateInterface updateInterface){
        this.mContext = context;
        this.mListener = updateInterface;
        mProgressDialog = new ProgressDialog(mContext);
        mSettings = new Settings(mContext);

        if (Launcher.mDataBaseJournal!=null){
            mDataBaseJournal = Launcher.mDataBaseJournal;
        } else {
            mDataBaseJournal = new DataBaseJournal(mContext);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Загрузка с сервера...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            Connection connection = SQL_Connection.SQLconnect;
            if (connection!=null){

                mDataBaseJournal.clearJournalDB();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM Journal_recycler");
                while (result.next()){
                    mDataBaseJournal.writeInDBJournal(new JournalItem()
                    .setAccountID(result.getString("ACCOUNT_ID"))
                    .setAuditroom(result.getString("AUDITROOM"))
                    .setTimeIn(Long.parseLong(result.getString("TIME_IN")))
                    .setTimeOut(Long.parseLong(result.getString("TIME_OUT")))
                    .setAccessType(result.getInt("ACCESS"))
                    .setPersonLastname(result.getString("PERSON_LASTNAME"))
                    .setPersonFirstname(result.getString("PERSON_FIRSTNAME"))
                    .setPersonMidname(result.getString("PERSON_MIDNAME"))
                    .setPersonPhoto(result.getString("PERSON_PHOTO")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
        mListener.updateInformation();
    }
}
