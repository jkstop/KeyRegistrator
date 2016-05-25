package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

import java.util.ArrayList;

/**
 * Получение пользователя из базы; создание journalItem, запись в журнал посещений; запись в БД помещений
 */
public class BaseWriter extends AsyncTask<BaseWriterParams,Void,Void> {

    private Context mContext;
    private BaseWriterInterface mBaseWriterInterface;

    private JournalItem mJournalItem;
    private RoomItem mRoomItem;


    public BaseWriter(Context context, BaseWriterInterface baseWriterInterface){
        mContext = context;
        mBaseWriterInterface = baseWriterInterface;
    }

    @Override
    protected void onPreExecute() {
        System.out.println("base writer ******************************************************");
        //Toasts.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_take_key), Toasts.TOAST_POSITIVE);
    }

    @Override
    protected Void doInBackground(BaseWriterParams... params) {
        System.out.println("person tag " + params[0].getPersonTag());

        PersonItem person = FavoriteDB.getPersonItem(params[0].getPersonTag(), FavoriteDB.LOCAL_USER, false);
        System.out.println("PERSON " + person);

        final long timeIn = System.currentTimeMillis();

        mJournalItem = new JournalItem()
                .setAccountID(Settings.getActiveAccountID())
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

        Settings.setLAstRoomTimeIn(mJournalItem.getTimeIn());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        System.out.println("base writer -------------------------------------");
        if (mBaseWriterInterface!=null) mBaseWriterInterface.onSuccessBaseWrite();

        if (Settings.getWriteServerStatus()){

            ArrayList<String> selectedItemsForWrite = Settings.getWriteServerItems();
            String[] allItemsForWrite = App.getAppContext().getResources().getStringArray(R.array.shared_preferences_write_server_items_entries);

            if (selectedItemsForWrite.contains(allItemsForWrite[0])){ //если выбран журнал
                new ServerWriter(mJournalItem).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.JOURNAL_UPDATE);
            }

            if (selectedItemsForWrite.contains(allItemsForWrite[2])){ //если выбраны помещения
                new ServerWriter(mRoomItem).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.ROOMS_UPDATE);
            }
        }
    }
}
