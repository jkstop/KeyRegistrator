package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Загрузка с сервера
 */
public class Load_from_server extends AsyncTask<Void,Void,Void> {

    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;

    public Load_from_server(Context context, UpdateInterface updateInterface){
        this.mListener = updateInterface;
        mProgressDialog = new ProgressDialog(context);

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

                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM JOURNAL ORDER BY TIME_IN ASC");
                while (result.next()){
                    if (JournalDB.getJournalItem(result.getLong("TIME_IN")) == null){
                        JournalDB.writeInDBJournal(new JournalItem()
                                .setAccountID(result.getString("ACCOUNT_ID"))
                                .setAuditroom(result.getString("AUDITROOM"))
                                .setTimeIn(result.getLong("TIME_IN"))
                                .setTimeOut(result.getLong("TIME_OUT"))
                                .setAccessType(result.getInt("ACCESS"))
                                .setPersonLastname(result.getString("PERSON_LASTNAME"))
                                .setPersonFirstname(result.getString("PERSON_FIRSTNAME"))
                                .setPersonMidname(result.getString("PERSON_MIDNAME"))
                                .setPersonPhoto(result.getString("PERSON_PHOTO")));
                    }
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
