package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

import java.util.ArrayList;

/**
 * Task for close room
 */
public class CloseRooms extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private String mTag;
    private CloseRoomInterface mCloseRoomInterface;
    private RoomItem mRoomItemUpdated;
    private long mRoomTime;

    public CloseRooms (Context context, String tag, CloseRoomInterface closeRoomInterface){
        mContext = context;
        mTag = tag;
        mCloseRoomInterface = closeRoomInterface;
    }

    @Override
    protected void onPreExecute() {
        System.out.println("close rooms ***********************************");
        Toasts.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_thanks), Toasts.TOAST_POSITIVE);
    }

    @Override
    protected Integer doInBackground(Void... params) {

        mRoomItemUpdated = RoomDB.getRoomItemForCurrentUser(mTag);
        mRoomTime = mRoomItemUpdated.getTime();

        JournalDB.updateDB(mRoomItemUpdated.getTime(), System.currentTimeMillis());

        return RoomDB.updateRoom(mRoomItemUpdated
                .setTime(0)
                .setLastVisiter(Values.EMPTY)
                .setTag(Values.EMPTY)
                .setStatus(RoomDB.ROOM_IS_FREE));
    }

    @Override
    protected void onPostExecute(Integer closedRooms) {
        System.out.println("close rooms --------------------------------------");
        if (mCloseRoomInterface !=null){
            mCloseRoomInterface.onRoomClosed();
        }

        if (Settings.getWriteServerStatus()){
            ArrayList<String> selectedItemsForWrite = Settings.getWriteServerItems(); //выбранные элементы для синхронизации
            String[] allItemsForWrite = App.getAppContext().getResources().getStringArray(R.array.shared_preferences_write_server_items_entries); //все элементы синхронизации

            if (selectedItemsForWrite.contains(allItemsForWrite[0])){ //если выбран журнал
                new ServerWriter(new JournalItem().setTimeIn(mRoomTime)).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.JOURNAL_UPDATE);
            }

            if (selectedItemsForWrite.contains(allItemsForWrite[2])){ //если выбраны помещения
                new ServerWriter(mRoomItemUpdated).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.ROOMS_UPDATE);
            }
        }
    }
}
