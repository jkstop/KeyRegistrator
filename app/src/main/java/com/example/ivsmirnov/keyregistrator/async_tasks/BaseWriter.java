package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

import java.sql.Connection;

/**
 * Получение пользователя из базы; создание journalItem, запись в журнал посещений; запись в БД помещений
 */
public class BaseWriter extends AsyncTask<BaseWriterParams,Void,Exception> implements SQL_Connection.Callback{

    public static final int WRITE_NEW = 100;
    public static final int UPDATE_CURRENT = 200;

    private Context mContext;
    private Callback mCallback;
    private int mType;

    private JournalItem mJournalItem;
    private RoomItem mRoomItem;
    private long mRoomItemTimeIn;


    public BaseWriter(int type, Context context, Callback callback){
        mContext = context;
        mCallback = callback;
        mType = type;
    }

    @Override
    protected void onPreExecute() {
        switch (mType){
            case WRITE_NEW:
                Toasts.handler.sendEmptyMessage(Toasts.TOAST_TAKE_KEY);
                break;
            case UPDATE_CURRENT:
                Toasts.handler.sendEmptyMessage(Toasts.TOAST_THANKS);
                break;
            default:
                break;
        }
    }

    @Override
    protected Exception doInBackground(BaseWriterParams... params) {
        try {
            switch (mType){
                case WRITE_NEW:
                    System.out.println("person tag " + params[0].getPersonTag());

                    PersonItem person = FavoriteDB.getPersonItem(params[0].getPersonTag(), false);
                    System.out.println("PERSON " + person);

                    final long timeIn = System.currentTimeMillis();

                    mJournalItem = new JournalItem()
                            .setAccountID(SharedPrefs.getActiveAccountID())
                            .setAuditroom(params[0].getAuditroom())
                            .setAccessType(params[0].getAccessType())
                            .setTimeIn(timeIn)
                            .setPersonInitials(FavoriteDB.getPersonInitials(FavoriteDB.FULL_INITIALS, person.getLastname(), person.getFirstname(), person.getMidname()))
                            .setPersonTag(person.getRadioLabel());

                    mRoomItem = new RoomItem()
                            .setAuditroom(mJournalItem.getAuditroom())
                            .setStatus(RoomDB.ROOM_IS_BUSY)
                            .setAccessType(mJournalItem.getAccessType())
                            .setTime(timeIn)
                            .setLastVisiter(FavoriteDB.getPersonInitials(FavoriteDB.SHORT_INITIALS, person.getLastname(), person.getFirstname(), person.getMidname()))
                            .setTag(params[0].getPersonTag());

                    JournalDB.writeInDBJournal(mJournalItem);
                    RoomDB.updateRoom(mRoomItem);
                    break;
                case UPDATE_CURRENT:
                    mRoomItem = RoomDB.getRoomItemForCurrentUser(params[0].getPersonTag());
                    mRoomItemTimeIn = mRoomItem.getTime();

                    JournalDB.updateDB(mRoomItemTimeIn, System.currentTimeMillis());

                    RoomDB.updateRoom(mRoomItem
                            .setTime(0)
                            .setLastVisiter("")
                            .setTag("")
                            .setStatus(RoomDB.ROOM_IS_FREE));
                    break;
                default:
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
            return e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Exception e) {
        if (e == null){
            if (mCallback!=null) mCallback.onSuccessBaseWrite();
            if (SharedPrefs.getWriteServerStatus()){
                SQL_Connection.getConnection(null, mType, this);
            }
        } else {
            if (mCallback!=null) mCallback.onErrorBaseWrite();
        }
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        switch (callingTask){
            case WRITE_NEW:
                new ServerWriter(ServerWriter.JOURNAL_UPDATE, mJournalItem, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                new ServerWriter(ServerWriter.ROOMS_UPDATE, mRoomItem, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                break;
            case UPDATE_CURRENT:
                new ServerWriter(ServerWriter.JOURNAL_UPDATE, new JournalItem().setTimeIn(mRoomItemTimeIn), null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                new ServerWriter(ServerWriter.ROOMS_UPDATE, mRoomItem, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerConnectException(Exception e) {
        System.out.println("SERVER IS DISCONNECT!!!");
    }

    public interface Callback{
        void onSuccessBaseWrite();
        void onErrorBaseWrite();
    }

}
