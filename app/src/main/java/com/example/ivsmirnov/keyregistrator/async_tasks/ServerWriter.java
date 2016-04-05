package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Запись на сервер
 */
public class ServerWriter extends AsyncTask<Integer,Void,Void> {

    public static final int JOURNAL_NEW = 100; //для добавления новой записи
    public static final int JOURNAL_ALL = 101; //для добавления всех не синхронизированных записей
    public static final int JOURNAL_DELETE_ONE = 102; //для удаления 1 записи
    public static final int JOURNAL_DELETE_ALL = 103; //для очистки все журнала
    public static final int PERSON_NEW = 200; //добавление 1 нового пользователя
    public static final int ROOMS = 3;

    public static final String PERSONS_TABLE = "TEACHERS"; //таблица с пользователями
    public static final String PERSONS_TABLE_COLUMN_RADIO_LABEL = "RADIO_LABEL";

    private ProgressDialog mProgressDialog;

    private Long mJournalItemTag;
    private JournalItem mJournalItem;
    private PersonItem mPersonItem;
    private RoomItem mRoomItem;

    public ServerWriter (JournalItem journalItem, Context context, boolean isShowProgress){
        mJournalItem = journalItem;
        if (isShowProgress) mProgressDialog = new ProgressDialog(context);
    }

    public ServerWriter (Long journalItemTag){
        mJournalItemTag = journalItemTag;
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
        System.out.println("start server writer");
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
                        if (mJournalItem!=null){
                            mResult = mStatement.executeQuery("SELECT * FROM JOURNAL WHERE [TIME_IN] = " + mJournalItem.getTimeIn());
                            mResult.first();
                            if (mResult.getRow() == 0){
                                writeJournalItem(mStatement, mJournalItem);
                            } else {
                                mResult.updateLong("TIME_OUT",System.currentTimeMillis());
                                mResult.updateRow();
                            }
                        }
                        break;
                    case JOURNAL_ALL:
                        ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);
                        for (Long tag : mJournalTags){
                            mResult = mStatement.executeQuery("SELECT TIME_IN FROM JOURNAL WHERE TIME_IN = " + tag);
                            mResult.first();
                            if (mResult.getRow()==0){
                                    mJournalItem = JournalDB.getJournalItem(tag);
                                    writeJournalItem(mStatement, mJournalItem);
                            }
                        }
                        break;
                    case JOURNAL_DELETE_ONE:
                        if (mJournalItemTag!=null){
                            mStatement.execute("DELETE FROM JOURNAL WHERE [TIME_IN] = " + mJournalItemTag);
                        }
                        break;
                    case JOURNAL_DELETE_ALL:
                        mStatement.execute("TRUNCATE TABLE JOURNAL");
                        break;
                    case PERSON_NEW:
                        if (mPersonItem!=null){
                            mResult = mStatement.executeQuery("SELECT * FROM " + PERSONS_TABLE + " WHERE " + PERSONS_TABLE_COLUMN_RADIO_LABEL + " = '" + mPersonItem.getRadioLabel() + "'");
                            mResult.first();
                            if (mResult.getRow() == 1){ //если такой уже есть, то удаляем
                                System.out.println("user already exist");
                                mStatement.executeUpdate("DELETE FROM " + PERSONS_TABLE + " WHERE " + PERSONS_TABLE_COLUMN_RADIO_LABEL + " = '" + mPersonItem.getRadioLabel() + "'");
                            }
                            System.out.println("write user");
                            mStatement.executeUpdate("INSERT INTO " + PERSONS_TABLE + " VALUES ('"
                                    +mPersonItem.getLastname() + "','"
                                    +mPersonItem.getFirstname() + "','"
                                    +mPersonItem.getMidname() + "','"
                                    +mPersonItem.getDivision() + "','"
                                    +mPersonItem.getRadioLabel() + "','"
                                    +mPersonItem.getSex() + "','"
                                    +mPersonItem.getPhotoPreview() + "','"
                                    +mPersonItem.getPhotoOriginal() + "')");
                        }
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

    private void writeJournalItem(Statement statement, JournalItem journalItem){
        try {
            statement.executeUpdate("INSERT INTO JOURNAL VALUES ('"
                    +journalItem.getAccountID()+"','"
                    +journalItem.getAuditroom()+"',"
                    +journalItem.getTimeIn()+","
                    +journalItem.getTimeOut()+","
                    +journalItem.getAccessType()+",'"
                    +journalItem.getPersonLastname()+"','"
                    +journalItem.getPersonFirstname()+"','"
                    +journalItem.getPersonMidname()+"','"
                    +journalItem.getPersonPhoto()+"')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mProgressDialog!=null && mProgressDialog.isShowing()) mProgressDialog.cancel();
    }
}
