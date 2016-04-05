package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Загрузка с сервера
 */
public class ServerLoader extends AsyncTask<Void,Void,Void> {

    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;

    public ServerLoader(Context context, UpdateInterface updateInterface){
        this.mListener = updateInterface;
        mProgressDialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("start server loader");
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

                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

                ResultSet preResult = connection.prepareStatement("SELECT TIME_IN FROM JOURNAL ORDER BY TIME_IN ASC").executeQuery();
                ResultSet postResult;
                ResultSet resultSetOpen;

                //получаем тэги (время входа) всех помещений с сервера
                while (preResult.next()){
                    long timeIn = preResult.getLong("TIME_IN");
                    if (!JournalDB.isJournalItemCreated(timeIn)){
                        //если записи нет в локальном журнале, то загружаем ее с сервера и пишем в журнал
                        postResult = statement.executeQuery("SELECT * FROM JOURNAL WHERE TIME_IN = " + timeIn);
                        while (postResult.next()){
                            JournalDB.writeInDBJournal(new JournalItem()
                                    .setAccountID(postResult.getString("ACCOUNT_ID"))
                                    .setAuditroom(postResult.getString("AUDITROOM"))
                                    .setTimeIn(postResult.getLong("TIME_IN"))
                                    .setTimeOut(postResult.getLong("TIME_OUT"))
                                    .setAccessType(postResult.getInt("ACCESS"))
                                    .setPersonLastname(postResult.getString("PERSON_LASTNAME"))
                                    .setPersonFirstname(postResult.getString("PERSON_FIRSTNAME"))
                                    .setPersonMidname(postResult.getString("PERSON_MIDNAME"))
                                    .setPersonPhoto(postResult.getString("PERSON_PHOTO")));
                        }
                    }
                }

                //проверяем открытые помещения. Если на сервере они закрыты, то обновляем локальный журнал.
                //сначала получаем тэги открытых помещений  в журнале (они же время входа)
                ArrayList<Long> mOpenTags = JournalDB.getOpenRoomsTags();
                //для каждого помещения проверяем, закрылось ли на сервере
                for (long timeIn : mOpenTags){
                    resultSetOpen = connection.prepareStatement("SELECT TIME_OUT FROM JOURNAL WHERE TIME_IN = " + timeIn).executeQuery();
                    while (resultSetOpen.next()){
                        long timeOut = resultSetOpen.getLong("TIME_OUT");
                        //если помещение закрыто на сервере, то обновляем журнал
                        if (timeOut!=0){
                            JournalDB.updateDB(timeIn, timeOut);
                        }
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
