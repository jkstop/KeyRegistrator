package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

/**
 * Task for close room
 */
public class CloseRooms extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private String mTag;
    private CloseRoomInterface mCloseRoomInterface;
    private RoomItem mRoomItem;

    public CloseRooms (Context context, String tag, CloseRoomInterface closeRoomInterface){
        mContext = context;
        mTag = tag;
        mCloseRoomInterface = closeRoomInterface;
    }

    @Override
    protected void onPreExecute() {
        Toasts.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_thanks), Toasts.TOAST_POSITIVE);
    }

    @Override
    protected Integer doInBackground(Void... params) {

        mRoomItem = RoomDB.getRoomItemForCurrentUser(mTag);
        int closedRooms = RoomDB.updateRoom(mRoomItem
                .setTag(Values.EMPTY)
                .setStatus(RoomDB.ROOM_IS_FREE));
        JournalDB.updateDB(mRoomItem.getTime());

        return closedRooms;
    }

    @Override
    protected void onPostExecute(Integer closedRooms) {
        if (mCloseRoomInterface !=null){
            mCloseRoomInterface.onRoomClosed();
        }

        if (Settings.getWriteServerStatus()){
            if (Settings.getWriteJournalServerStatus()) new ServerWriter(new JournalItem().setTimeIn(mRoomItem.getTime()), null, false).execute(ServerWriter.JOURNAL_NEW);
        }
    }
}
