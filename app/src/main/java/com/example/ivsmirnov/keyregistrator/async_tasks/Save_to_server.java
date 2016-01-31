package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.custom_views.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 10.12.2015.
 */
public class Save_to_server extends AsyncTask <Void,Void,Void> {

    private Context mContext;
    private Settings mSettings;
    private ProgressDialog mProgressDialog;

    public Save_to_server (Context context){
        this.mContext = context;
        mSettings = new Settings(mContext);
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
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        ArrayList<JournalItem> mItems = dbJournal.realAllJournalFromDB();

        try {
            Connection connection = SQL_Connector.check_sql_connection(mContext, mSettings.getServerConnectionParams());
            if (connection!=null){
                PreparedStatement trunacteTable = connection.prepareStatement("TRUNCATE TABLE Journal_recycler");
                trunacteTable.execute();
                for (int i=0;i<mItems.size();i++){
                    JournalItem journalItem = mItems.get(i);
                    PreparedStatement preparedStatement  = connection.prepareStatement("INSERT INTO Journal_recycler VALUES ('"
                            +journalItem.Auditroom+"','"
                            +journalItem.TimeIn+"','"
                            +journalItem.TimeOut+"',"
                            +journalItem.AccessType+",'"
                            +journalItem.PersonLastname+"','"
                            +journalItem.PersonFirstname+"','"
                            +journalItem.PersonMidname+"','"
                            +journalItem.PersonPhoto+"')");
                    preparedStatement.execute();
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
