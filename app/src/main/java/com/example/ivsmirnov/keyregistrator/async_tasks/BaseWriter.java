package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
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
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

/**
 * Получение пользователя из базы; создание journalItem, запись в журнал посещений; запись в БД помещений
 */
public class BaseWriter extends AsyncTask<BaseWriterParams,Void,Void> {

    private Context mContext;
    private BaseWriterInterface mBaseWriterInterface;

    private JournalItem mJournalItem;


    public BaseWriter(Context context, BaseWriterInterface baseWriterInterface){
        mContext = context;
        mBaseWriterInterface = baseWriterInterface;
    }

    @Override
    protected void onPreExecute() {
        Toasts.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_take_key), Toasts.TOAST_POSITIVE);
    }

    @Override
    protected Void doInBackground(BaseWriterParams... params) {

        PersonItem person = FavoriteDB.getPersonItem(params[0].getPersonTag(), FavoriteDB.LOCAL_USER, FavoriteDB.PREVIEW_PHOTO);

        final long timeIn = System.currentTimeMillis();

        mJournalItem = new JournalItem()
                .setAccountID(Settings.getActiveAccountID())
                .setAuditroom(params[0].getAuditroom())
                .setAccessType(params[0].getAccessType())
                .setTimeIn(timeIn)
                .setPersonLastname(person.getLastname())
                .setPersonFirstname(person.getFirstname())
                .setPersonMidname(person.getMidname())
                .setPersonPhoto(person.getPhotoPreview());

        JournalDB.writeInDBJournal(mJournalItem);

        RoomDB.updateRoom(new RoomItem()
                .setAuditroom(mJournalItem.getAuditroom())
                .setStatus(RoomDB.ROOM_IS_BUSY)
                .setAccessType(mJournalItem.getAccessType())
                .setTime(timeIn)
                .setLastVisiter(FavoriteDB.getPersonInitials(FavoriteDB.SHORT_INITIALS, mJournalItem.getPersonLastname(),mJournalItem.getPersonFirstname(),mJournalItem.getPersonMidname()))
                .setTag(params[0].getPersonTag()));

        Settings.setLAstRoomTimeIn(mJournalItem.getTimeIn());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mBaseWriterInterface!=null) mBaseWriterInterface.onSuccessBaseWrite();

        if (Settings.getWriteServerStatus()){
            if (Settings.getWriteJournalServerStatus()) new ServerWriter(mJournalItem, null, false).execute(ServerWriter.JOURNAL_NEW);
        }
    }
}
