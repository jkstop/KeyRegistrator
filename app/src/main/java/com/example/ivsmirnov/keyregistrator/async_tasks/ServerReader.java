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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Загрузка с сервера
 */
public class ServerReader extends AsyncTask<Integer,Integer,Void> {

    public static final int LOAD_JOURNAL = 100;
    public static final int LOAD_TEACHERS = 200;
    public static final int LOAD_ROOMS = 300;

    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;

    public ServerReader(Context context, UpdateInterface updateInterface){
        this.mListener = updateInterface;
        mProgressDialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("server reader *******************************");
        if (mProgressDialog!=null){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMax(0);
            mProgressDialog.setMessage("Синхронизация...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Integer... params) {

        try {
            Connection connection = SQL_Connection.getConnection(null, null);
            if (connection!=null){

                Statement mStatement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet mResult;
                switch (params[0]){
                    case LOAD_JOURNAL:
                        ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);

                        //если есть диалог, то получаем счетчик всех записей и ставим значение как максимум
                        if (mProgressDialog!=null){
                            ResultSet getCountResult = connection.prepareStatement("SELECT " + SQL_Connection.COLUMN_JOURNAL_TIME_IN
                                            + " FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN
                                            + " NOT IN (" + getInClause(mJournalTags) + ")"
                                    , ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
                                    .executeQuery();
                            getCountResult.last();
                            mProgressDialog.setMax(getCountResult.getRow());
                        }

                        //выбираем тэги записей, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                        //можно было бы получить сразу всю строчку целиком, а не только тэги, но тогда все зависает нафиг
                        ResultSet getJournalTagsResult = connection.prepareStatement("SELECT " + SQL_Connection.COLUMN_JOURNAL_TIME_IN
                                + " FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN
                                + " NOT IN (" + getInClause(mJournalTags) + ")"
                                ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();
                        ResultSet journalItemResult;

                        while (getJournalTagsResult.next()){
                               //для каждого тэга получаем journalItem и пишем в устройство
                                journalItemResult = mStatement.executeQuery("SELECT * FROM " +SQL_Connection.JOURNAL_TABLE
                                        + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " = " + getJournalTagsResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN));

                                journalItemResult.first();
                                if (journalItemResult.getRow() != 0){
                                    JournalDB.writeInDBJournal(new JournalItem()
                                            .setAccountID(journalItemResult.getString(SQL_Connection.COLUMN_JOURNAL_ACCOUNT_ID))
                                            .setAuditroom(journalItemResult.getString(SQL_Connection.COLUMN_JOURNAL_ROOM))
                                            .setTimeIn(journalItemResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN))
                                            .setTimeOut(journalItemResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_OUT))
                                            .setAccessType(journalItemResult.getInt(SQL_Connection.COLUMN_JOURNAL_ACCESS))
                                            .setPersonInitials(journalItemResult.getString(SQL_Connection.COLUMN_JOURNAL_PERSON_INITIALS))
                                            .setPersonTag(journalItemResult.getString(SQL_Connection.COLUMN_JOURNAL_PERSON_TAG)));
                                }

                            publishProgress(getJournalTagsResult.getRow());
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

                        //получаем число отсутствующих в устройстве записей и устанавливаем это значение как MAX в диалоге
                        if (mProgressDialog!=null){
                            ResultSet getCountResult = connection.prepareStatement("SELECT " + SQL_Connection.COLUMN_PERSONS_TAG + " FROM " + SQL_Connection.PERSONS_TABLE
                                            + " WHERE " + SQL_Connection.COLUMN_PERSONS_TAG + " NOT IN (" + getInClause(mPersonsTags) + ")"
                                    ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();
                            getCountResult.last();
                            mProgressDialog.setMax(getCountResult.getRow());

                        }

                        //выбираем тэги записей, которые есть на сервере, но нет в устройстве. Получаем по тэгу запись,пишем в устройство
                        //можно было бы получить сразу всю строчку целиком, а не только тэги, но тогда все зависает нафиг
                        ResultSet getPesonsTagsResult = connection.prepareStatement("SELECT " + SQL_Connection.COLUMN_PERSONS_TAG + " FROM " + SQL_Connection.PERSONS_TABLE
                                + " WHERE " + SQL_Connection.COLUMN_PERSONS_TAG + " NOT IN (" + getInClause(mPersonsTags) + ")"
                                ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();
                        ResultSet personItemResult;

                        while (getPesonsTagsResult.next()){
                            personItemResult = mStatement.executeQuery("SELECT * FROM " +SQL_Connection.PERSONS_TABLE
                                    + " WHERE " + SQL_Connection.COLUMN_PERSONS_TAG
                                    + " = '" + getPesonsTagsResult.getString(SQL_Connection.COLUMN_PERSONS_TAG) + "'");
                            personItemResult.first();
                            if (personItemResult.getRow() != 0){
                                FavoriteDB.addNewUser(new PersonItem()
                                        .setLastname(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_LASTNAME))
                                        .setFirstname(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_FIRSTNAME))
                                        .setMidname(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_MIDNAME))
                                        .setDivision(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_DIVISION))
                                        .setRadioLabel(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_TAG))
                                        .setSex(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_SEX))
                                        .setPhoto(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_PHOTO_BASE64)));
                            }

                            publishProgress(getPesonsTagsResult.getRow());
                        }
                        break;
                    case LOAD_ROOMS:
                        //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                        ArrayList<String> mRooms = RoomDB.getRoomList();
                        mResult = connection.prepareStatement("SELECT * FROM " + SQL_Connection.ROOMS_TABLE
                                + " WHERE " + SQL_Connection.COLUMN_ROOMS_ROOM + " NOT IN (" + getInClause(mRooms) + ")").executeQuery();

                        while (mResult.next()){
                            RoomDB.writeInRoomsDB(new RoomItem()
                                    .setAuditroom(mResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM))
                                    .setStatus(mResult.getInt(SQL_Connection.COLUMN_ROOMS_STATUS))
                                    .setAccessType(mResult.getInt(SQL_Connection.COLUMN_ROOMS_ACCESS))
                                    .setTime(mResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME))
                                    .setLastVisiter(mResult.getString(SQL_Connection.COLUMN_ROOMS_LAST_VISITER))
                                    .setTag(mResult.getString(SQL_Connection.COLUMN_ROOMS_RADIO_LABEL)));
                        }

                        //для всех записей проверяем статус. Если на устройстве не совпадает с сервером, пишем в устройство новый статус
                        //сначала получаем с сервера список всех помещений и их статусы
                        mResult = mStatement.executeQuery("SELECT * FROM " + SQL_Connection.ROOMS_TABLE);
                        //для каждого значения сравниваем статус с локальным
                        String aud;
                        int status;
                        long timeServer;
                        long timeLocal;
                        while (mResult.next()){
                            aud = mResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM);
                            status = mResult.getInt(SQL_Connection.COLUMN_ROOMS_STATUS);
                            //статус не совпал, пишем в устройство новый статус
                            if (status != RoomDB.getRoomStatus(aud)){
                                switch (status){
                                    case RoomDB.ROOM_IS_FREE: //освобождаем помещение
                                        RoomDB.updateRoom(new RoomItem()
                                                .setAuditroom(aud)
                                                .setStatus(status));
                                        break;
                                    case RoomDB.ROOM_IS_BUSY: //занимаем помещение
                                        RoomDB.updateRoom(new RoomItem()
                                                .setAuditroom(aud)
                                                .setStatus(status)
                                                .setTag(mResult.getString(SQL_Connection.COLUMN_ROOMS_RADIO_LABEL))
                                                .setAccessType(mResult.getInt(SQL_Connection.COLUMN_ROOMS_ACCESS))
                                                .setLastVisiter(mResult.getString(SQL_Connection.COLUMN_ROOMS_LAST_VISITER))
                                                .setTime(mResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME)));
                                        break;
                                    default:
                                        break;
                                }
                            } else { //статус совпал, проверяем время входа
                                timeServer = mResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME);
                                timeLocal = RoomDB.getRoomTimeIn(aud);
                                if (timeServer != timeLocal){ //время отличается, обновляем на устройстве
                                    RoomDB.updateRoom(new RoomItem()
                                            .setAuditroom(aud)
                                            .setStatus(status)
                                            .setTag(mResult.getString(SQL_Connection.COLUMN_ROOMS_RADIO_LABEL))
                                            .setAccessType(mResult.getInt(SQL_Connection.COLUMN_ROOMS_ACCESS))
                                            .setLastVisiter(mResult.getString(SQL_Connection.COLUMN_ROOMS_LAST_VISITER))
                                            .setTime(mResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME)));
                                }
                            }
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

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (mProgressDialog!=null) mProgressDialog.setProgress(values[0]);
    }

    private String getInClause(ArrayList items){
        StringBuilder inClause = new StringBuilder();
        if (items.size() != 0){
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
        } else {
            return "'0'";
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        System.out.println("server reader -----------------------------");
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
        if (mListener!=null) mListener.updateInformation();
    }
}
