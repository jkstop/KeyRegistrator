package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Загрузка с сервера
 */
public class ServerLoader extends AsyncTask<Integer,Void,Void> {

    public static final int LOAD_JOURNAL = 100;
    public static final int LOAD_TEACHERS = 200;
    public static final int LOAD_ROOMS = 300;

    private long taskDurationStart, taskDurationEnd;

    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;

    public ServerLoader(Context context, UpdateInterface updateInterface){
        this.mListener = updateInterface;
        mProgressDialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        taskDurationStart = System.currentTimeMillis();
        System.out.println("start server loader");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Загрузка с сервера...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Integer... params) {

        try {
            Connection connection = SQL_Connection.SQLconnect;
            if (connection!=null){

                Statement mStatement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet mResult;
                switch (params[0]){
                    case LOAD_JOURNAL:
                        ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);

                        //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                        mResult = mStatement.executeQuery("SELECT * FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " NOT IN (" + getInClause(mJournalTags) + ")");
                        while (mResult.next()){
                            JournalDB.writeInDBJournal(new JournalItem()
                                    .setAccountID(mResult.getString(SQL_Connection.COLUMN_JOURNAL_ACCOUNT_ID))
                                    .setAuditroom(mResult.getString(SQL_Connection.COLUMN_JOURNAL_AUDITROOM))
                                    .setTimeIn(mResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN))
                                    .setTimeOut(mResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_OUT))
                                    .setAccessType(mResult.getInt(SQL_Connection.COLUMN_JOURNAL_ACCESS))
                                    .setPersonLastname(mResult.getString(SQL_Connection.COLUMN_JOURNAL_LASTNAME))
                                    .setPersonFirstname(mResult.getString(SQL_Connection.COLUMN_JOURNAL_FIRSTNAME))
                                    .setPersonMidname(mResult.getString(SQL_Connection.COLUMN_JOURNAL_MIDNAME))
                                    .setPersonPhoto(mResult.getString(SQL_Connection.COLUMN_JOURNAL_PHOTO)));
                        }

                        //проверяем открытые помещения. Если на сервере они закрыты, то обновляем локальный журнал.
                        //сначала получаем тэги открытых помещений  в журнале (они же время входа)
                        ArrayList<Long> mOpenTags = JournalDB.getOpenRoomsTags();

                        //для каждого открытого помещения проверяем, закрылось ли на сервере
                        mResult = mStatement.executeQuery("SELECT " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + ","
                                + SQL_Connection.COLUMN_JOURNAL_TIME_OUT + " FROM " + SQL_Connection.JOURNAL_TABLE
                                + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " IN (" + getInClause(mOpenTags) + ")");
                        long timeOut;
                        long timeIn;
                        while (mResult.next()){
                            timeOut = mResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_OUT);
                            timeIn = mResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN);
                            if (timeOut!=0) JournalDB.updateDB(timeIn, timeOut);
                        }

                        break;
                    case LOAD_TEACHERS:
                        ArrayList<String> mPersonsTags = FavoriteDB.getPersonsTags();
                        //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                        mResult = mStatement.executeQuery("SELECT * FROM " + SQL_Connection.PERSONS_TABLE
                                + " WHERE " + SQL_Connection.COLUMN_PERSONS_RADIO_LABEL + " NOT IN (" + getInClause(mPersonsTags) + ")");
                        while (mResult.next()){
                            FavoriteDB.writeInDBTeachers(new PersonItem()
                                    .setLastname(mResult.getString(SQL_Connection.COLUMN_PERSONS_LASTNAME))
                                    .setFirstname(mResult.getString(SQL_Connection.COLUMN_PERSONS_FIRSTNAME))
                                    .setMidname(mResult.getString(SQL_Connection.COLUMN_PERSONS_MIDNAME))
                                    .setDivision(mResult.getString(SQL_Connection.COLUMN_PERSONS_DIVISION))
                                    .setRadioLabel(mResult.getString(SQL_Connection.COLUMN_PERSONS_RADIO_LABEL))
                                    .setSex(mResult.getString(SQL_Connection.COLUMN_PERSONS_SEX))
                                    .setPhotoPreview(mResult.getString(SQL_Connection.COLUMN_PERSONS_PHOTO_PREVIEW))
                                    .setPhotoOriginal(mResult.getString(SQL_Connection.COLUMN_PERSONS_PHOTO_ORIGINAL)));
                            System.out.println("write in local " + mResult.getString(SQL_Connection.COLUMN_PERSONS_LASTNAME));
                        }
                        break;
                    case LOAD_ROOMS:
                        //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                        ArrayList<String> mRooms = RoomDB.getRoomList();
                        mResult = mStatement.executeQuery("SELECT * FROM " + SQL_Connection.ROOMS_TABLE
                                + " WHERE " + SQL_Connection.COLUMN_ROOMS_ROOM + " NOT IN (" + getInClause(mRooms) + ")");
                        while (mResult.next()){
                            RoomDB.writeInRoomsDB(new RoomItem()
                                    .setAuditroom(mResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM))
                                    .setStatus(mResult.getInt(SQL_Connection.COLUMN_ROOMS_STATUS))
                                    .setAccessType(mResult.getInt(SQL_Connection.COLUMN_ROOMS_ACCESS))
                                    .setTime(mResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME))
                                    .setLastVisiter(mResult.getString(SQL_Connection.COLUMN_ROOMS_LAST_VISITER))
                                    .setTag(mResult.getString(SQL_Connection.COLUMN_ROOMS_RADIO_LABEL))
                                    .setPhoto(mResult.getString(SQL_Connection.COLUMN_ROOMS_PHOTO)));
                            System.out.println("write " + mResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM));
                        }
                        break;
                    default:
                        break;
                }


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getInClause(ArrayList items){
        StringBuilder inClause = new StringBuilder();
        for (int i=0; i < items.size(); i++) {
            if (items.get(i).getClass().equals(String.class)){
                inClause.append("'" + items.get(i) + "'");
            } else {
                inClause.append(items.get(i));
            }
            inClause.append(',');
        }
        if (inClause.length() == 0){
            inClause.append(0);
        } else {
            inClause.delete(inClause.length()-1,inClause.length());
        }
        return inClause.toString();
    }



    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        taskDurationEnd = System.currentTimeMillis();
        System.out.println("LOAD TASK DURATION " + (taskDurationEnd - taskDurationStart));
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
        if (mListener!=null) mListener.updateInformation();
    }
}
