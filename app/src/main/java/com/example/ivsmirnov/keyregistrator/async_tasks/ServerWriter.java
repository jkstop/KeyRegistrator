package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Запись на сервер
 */
public class ServerWriter extends AsyncTask<Integer,Void,Void> {

    public static final int JOURNAL_NEW = 0; //для добавления новой записи
    public static final int JOURNAL_ALL = 1; //для добавления всех не синхронизированных записей
    public static final int TEACHERS = 2;
    public static final int ROOMS = 3;

    private ProgressDialog mProgressDialog;

    private JournalItem mJournalItem;
    private PersonItem mPersonItem;
    private RoomItem mRoomItem;

    public ServerWriter (JournalItem journalItem, Context context, boolean isShowProgress){
        mJournalItem = journalItem;
        if (isShowProgress) mProgressDialog = new ProgressDialog(context);
    }

    public ServerWriter (PersonItem personItem){
        mPersonItem = personItem;
    }

    public ServerWriter (RoomItem roomItem){
        mRoomItem = roomItem;
    }

    public ServerWriter (){
    }

    public ServerWriter (Context context, boolean isShowProgress){
        if (isShowProgress) mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mProgressDialog!=null){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Запись на сервер...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Integer... params) {
        try {
            Connection mConnection = SQL_Connection.SQLconnect;
            Statement mStatement = mConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet mResult;
            if (mConnection!=null){
                switch (params[0]){
                    case JOURNAL_NEW:
                        mResult = mStatement.executeQuery("SELECT * FROM Journal_recycler WHERE [TIME_IN] = '" + mJournalItem.getTimeIn() + "'");
                        mResult.next();

                        if (mResult.getRow() == 0){
                            System.out.println("write");
                            mStatement.executeUpdate("INSERT INTO Journal_recycler VALUES ('"
                                    +mJournalItem.getAccountID()+"','"
                                    +mJournalItem.getAuditroom()+"','"
                                    +mJournalItem.getTimeIn()+"','"
                                    +mJournalItem.getTimeOut()+"',"
                                    +mJournalItem.getAccessType()+",'"
                                    +mJournalItem.getPersonLastname()+"','"
                                    +mJournalItem.getPersonFirstname()+"','"
                                    +mJournalItem.getPersonMidname()+"','"
                                    +mJournalItem.getPersonPhoto()+"')");
                        } else {
                            System.out.println("update");
                            mResult.updateLong("TIME_OUT",System.currentTimeMillis());
                            mResult.updateRow();
                        }
                        break;
                    case JOURNAL_ALL:
                        ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);
                        JournalItem journalItem;
                        for (Long tag : mJournalTags){
                            journalItem = JournalDB.getJournalItem(tag);
                            mResult = mStatement.executeQuery("SELECT * FROM Journal_recycler WHERE [TIME_IN] = '" + journalItem.getTimeIn() + "'");
                            mResult.next();
                            if (mResult.getRow()==0){
                                System.out.println("write "+journalItem.getAuditroom());
                                mStatement.executeUpdate("INSERT INTO Journal_recycler VALUES ('"
                                        +journalItem.getAccountID()+"','"
                                        +journalItem.getAuditroom()+"','"
                                        +journalItem.getTimeIn()+"','"
                                        +journalItem.getTimeOut()+"',"
                                        +journalItem.getAccessType()+",'"
                                        +journalItem.getPersonLastname()+"','"
                                        +journalItem.getPersonFirstname()+"','"
                                        +journalItem.getPersonMidname()+"','"
                                        +journalItem.getPersonPhoto()+"')");
                            }
                        }
                        break;
                    case TEACHERS:
                        break;
                    case ROOMS:
                        break;
                    default:
                        break;
                }

                if (mStatement!=null){
                    mStatement.close();
                }

            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mProgressDialog!=null && mProgressDialog.isShowing()) mProgressDialog.cancel();
    }
}
