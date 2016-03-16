package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
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

        PersonItem person = DataBaseFavorite.getPersonItem(mContext, params[0].getPersonTag(), DataBaseFavorite.LOCAL_USER, DataBaseFavorite.PREVIEW_PHOTO);

        JournalItem journalItem = new JournalItem()
                .setAccountID(Settings.getActiveAccountID())
                .setAuditroom(params[0].getAuditroom())
                .setAccessType(params[0].getAccessType())
                .setTimeIn(System.currentTimeMillis())
                .setPersonLastname(person.getLastname())
                .setPersonFirstname(person.getFirstname())
                .setPersonMidname(person.getMidname())
                .setPersonPhoto(person.getPhotoPreview());

        long positionInBase = DataBaseJournal.writeInDBJournal(journalItem);

        DataBaseRooms.updateRoom(new RoomItem()
                .setAuditroom(journalItem.getAuditroom())
                .setStatus(DataBaseRooms.ROOM_IS_BUSY)
                .setAccessType(journalItem.getAccessType())
                .setPositionInBase(positionInBase)
                .setLastVisiter(Persons_Fragment.getPersonInitials(journalItem.getPersonLastname(),journalItem.getPersonFirstname(),journalItem.getPersonMidname()))
                .setTag(params[0].getPersonTag()));
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mBaseWriterInterface!=null) mBaseWriterInterface.onSuccessBaseWrite();
    }
}