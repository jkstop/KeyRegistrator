package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Запись на сервер
 */
public class Save_to_server extends AsyncTask <Void,Void,Void> {

    private ProgressDialog mProgressDialog;
    private boolean mIsShowDialog;

    public Save_to_server (Context context, boolean isShowDialog){
        mProgressDialog = new ProgressDialog(context);
        mIsShowDialog = isShowDialog;
    }

    @Override
    protected void onPreExecute() {
        if (mIsShowDialog){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Запись на сервер...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        ArrayList<JournalItem> mItems = JournalDB.realAllJournalFromDB();

        try {
            Connection connection = SQL_Connection.SQLconnect;
            if (connection!=null){

                PreparedStatement trunacteTable = connection.prepareStatement("TRUNCATE TABLE Journal_recycler");
                trunacteTable.execute();
                trunacteTable.close();

                JournalItem journalItem;
                PreparedStatement preparedStatement;
                for (int i=0;i<mItems.size();i++){
                    journalItem = JournalDB.getJournalItem(mItems.get(i).getTimeIn());
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
