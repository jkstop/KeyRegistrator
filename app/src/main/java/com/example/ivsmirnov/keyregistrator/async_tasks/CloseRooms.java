package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

/**
 * Task for close room
 */
public class CloseRooms extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private String mTag;
    private CloseRoomInterface mCloseRoomInterface;

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

        RoomItem currentRoomItem = DataBaseRooms.getRoomItemForCurrentUser(mTag);
        int closedRooms = DataBaseRooms.updateRoom(currentRoomItem
                .setTag(Values.EMPTY)
                .setStatus(DataBaseRooms.ROOM_IS_FREE));
        DataBaseJournal.updateDB(currentRoomItem.getPositionInBase());

        return closedRooms;
    }

    @Override
    protected void onPostExecute(Integer closedRooms) {
        if (mCloseRoomInterface !=null){
            mCloseRoomInterface.onRoomClosed();
        }
    }
}
