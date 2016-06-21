package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Загрузка с сервера
 */
public class ServerReader extends AsyncTask<Connection,Integer,Exception> {

    public static final int READ_ALL = 10;
    public static final int READ_PERSON_ITEM = 20;

    private Handler mHandler;
    private static final int HANDLER_UPDATE_JOURNAL = 100;
    private static final int HANDLER_UPDATE_PERSONS = 101;
    private static final int HANDLER_UPDATE_ROOMS = 102;

    private ProgressDialog mProgressDialog;
    private Callback mCallback;

    private String mPersonItemTag;
    private PersonItem mServerPersonItem;
    private int mReadParam;

    private int dialogMaxCount = 0;

    public ServerReader(int readParam, Context context, Callback callback){
        mReadParam = readParam;
        mProgressDialog = new ProgressDialog(context);
        mCallback = callback;
    }

    public ServerReader(int readParam, String personTag, Callback callback){
        mReadParam =readParam;
        mPersonItemTag = personTag;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("start server reader");

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mProgressDialog!=null){
                    switch (msg.what){
                        case HANDLER_UPDATE_JOURNAL:
                            mProgressDialog.setMessage("Загрузка журнала");
                            break;
                        case HANDLER_UPDATE_PERSONS:
                            mProgressDialog.setMessage("Загрузка пользователей");
                            break;
                        case HANDLER_UPDATE_ROOMS:
                            mProgressDialog.setMessage("Загрузка помещений");
                            break;
                        default:
                            break;
                    }
                    mProgressDialog.setMax(dialogMaxCount);
                }
            }
        };

        if (mProgressDialog!=null){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage("Загрузка...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMax(0);
            mProgressDialog.show();
        }

    }

    @Override
    protected Exception doInBackground(Connection... params) {
        switch (mReadParam){
            case READ_ALL:
                try {
                    Connection connection = params[0];
                    Statement mStatement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    ResultSet mResult;
                    ResultSet journalItemResult;

                    //сначала обновляем журнал

                    //получаем список тэгов всех записей журнала на устройстве
                    ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);
                    //получаем счетчик всех отсутствующих записей и ставим значение как максимум
                    ResultSet getJournalTagsResult = connection.prepareStatement("SELECT " + SQL_Connection.COLUMN_JOURNAL_TIME_IN
                                    + " FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN
                                    + " NOT IN (" + getInClause(mJournalTags) + ")"
                            , ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
                            .executeQuery();
                    getJournalTagsResult.last();

                    dialogMaxCount = getJournalTagsResult.getRow();
                    mHandler.sendEmptyMessage(HANDLER_UPDATE_JOURNAL);


                    //двигаем в начало
                    getJournalTagsResult.beforeFirst();


                    while (getJournalTagsResult.next()){
                        //для каждого тэга получаем journalItem и пишем в устройство. Можно бы получить все сразу, но тогда зависает, данных много.
                        journalItemResult = mStatement.executeQuery("SELECT * FROM " +SQL_Connection.JOURNAL_TABLE
                                + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " = " + getJournalTagsResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN));

                        journalItemResult.first();
                        if (journalItemResult.getRow() != 0){
                            //пишем в журнал
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

                    //теперь проверяем пользователей

                    //список тэгов всех пользователей
                    ArrayList<String> mPersonsTags = FavoriteDB.getPersonsTags();
                    ResultSet personItemResult;

                    //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                    ResultSet getPesonsTagsResult = connection.prepareStatement("SELECT " + SQL_Connection.COLUMN_PERSONS_TAG + " FROM " + SQL_Connection.PERSONS_TABLE
                                    + " WHERE " + SQL_Connection.COLUMN_PERSONS_TAG + " NOT IN (" + getInClause(mPersonsTags) + ")"
                            ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();

                    getPesonsTagsResult.last();

                    dialogMaxCount = getPesonsTagsResult.getRow();
                    mHandler.sendEmptyMessage(HANDLER_UPDATE_PERSONS);

                    //в начало
                    getPesonsTagsResult.beforeFirst();

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
                                    .setAccessType(personItemResult.getInt(SQL_Connection.COLUMN_PERSONS_ACCESS))
                                    .setSex(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_SEX))
                                    .setPhoto(personItemResult.getString(SQL_Connection.COLUMN_PERSONS_PHOTO_BASE64)),
                                    false);
                        }

                        publishProgress(getPesonsTagsResult.getRow());
                    }

                    //помещения

                    //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                    ArrayList<String> mRooms = RoomDB.getRoomList();

                    ResultSet getRoomsItemsResult = connection.prepareStatement("SELECT * FROM " + SQL_Connection.ROOMS_TABLE
                                    + " WHERE " + SQL_Connection.COLUMN_ROOMS_ROOM + " NOT IN (" + getInClause(mRooms) + ")"
                            ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();

                    getRoomsItemsResult.last();

                    dialogMaxCount = getRoomsItemsResult.getRow();
                    mHandler.sendEmptyMessage(HANDLER_UPDATE_ROOMS);

                    //в начало
                    getRoomsItemsResult.beforeFirst();

                    while (getRoomsItemsResult.next()){

                        RoomDB.writeInRoomsDB(new RoomItem()
                                .setAuditroom(getRoomsItemsResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM))
                                .setStatus(getRoomsItemsResult.getInt(SQL_Connection.COLUMN_ROOMS_STATUS))
                                .setAccessType(getRoomsItemsResult.getInt(SQL_Connection.COLUMN_ROOMS_ACCESS))
                                .setTime(getRoomsItemsResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME))
                                .setLastVisiter(getRoomsItemsResult.getString(SQL_Connection.COLUMN_ROOMS_LAST_VISITER))
                                .setTag(getRoomsItemsResult.getString(SQL_Connection.COLUMN_ROOMS_RADIO_LABEL)));
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
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
                break;
            case READ_PERSON_ITEM:
                try {
                    Connection connection = params[0];
                    //получаем всю инфу с сервера о пользователе
                    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + SQL_Connection.ALL_STAFF_TABLE
                            + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_TAG + " = '" + mPersonItemTag + "'");
                    resultSet.first();
                    if (resultSet.getRow() !=0){
                        mServerPersonItem = new PersonItem().setLastname(resultSet.getString("LASTNAME"))
                                .setFirstname(resultSet.getString("FIRSTNAME"))
                                .setMidname(resultSet.getString("MIDNAME"))
                                .setDivision(resultSet.getString("NAME_DIVISION"))
                                .setSex(resultSet.getString("SEX"))
                                .setPhoto(resultSet.getString("PHOTO"))
                                .setRadioLabel(resultSet.getString("RADIO_LABEL"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
                break;
            default:
                break;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (mProgressDialog!=null){
            mProgressDialog.setProgress(values[0]);
        }
    }

    private String getInClause(ArrayList items){
        StringBuilder inClause = new StringBuilder();
        if (items.size() != 0){
            for (int i=0; i < items.size(); i++) {
                if (items.get(i).getClass().equals(String.class)){
                    inClause.append("'").append(items.get(i)).append("'");
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
    protected void onPostExecute(Exception e) {
        super.onPostExecute(e);

        if (mCallback!=null){
            if (e == null){
                if (mServerPersonItem!=null){
                    mCallback.onSuccessServerRead(mReadParam, mServerPersonItem);
                } else {
                    mCallback.onSuccessServerRead(mReadParam, null);
                }
            }else{
                mCallback.onErrorServerRead(e);
            }
        }
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }

    public interface Callback{
        void onSuccessServerRead(int task, Object result);
        void onErrorServerRead(Exception e);
    }

}
