package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 10.12.2015.
 */
public class Save_to_server extends AsyncTask <Void,Void,Void> {

    private Context mContext;
    private ProgressDialog mProgressDialog;

    public Save_to_server (Context context){
        this.mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Запись на сервер...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {

        ArrayList<JournalItem> mItems = DataBaseJournal.realAllJournalFromDB();

        try {
            Connection connection = SQL_Connection.SQLconnect;
            if (connection!=null){

                PreparedStatement trunacteTable = connection.prepareStatement("TRUNCATE TABLE Journal_recycler");
                trunacteTable.execute();
                trunacteTable.close();

                JournalItem journalItem;
                PreparedStatement preparedStatement;
                for (int i=0;i<mItems.size();i++){
                    journalItem = DataBaseJournal.getJournalItem(mItems.get(i).getTimeIn());
                    preparedStatement  = connection.prepareStatement("INSERT INTO Journal_recycler VALUES ('"
                            +journalItem.getAccountID()+"','"
                            +journalItem.getAuditroom()+"','"
                            +journalItem.getTimeIn()+"','"
                            +journalItem.getTimeOut()+"',"
                            +journalItem.getAccessType()+",'"
                            +journalItem.getPersonLastname()+"','"
                            +journalItem.getPersonFirstname()+"','"
                            +journalItem.getPersonMidname()+"','"
                            +journalItem.getPersonPhoto()+"')");
                    preparedStatement.execute();
                    preparedStatement.close();
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
    }
}
