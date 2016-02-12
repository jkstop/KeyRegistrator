package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.RoomInterface;
import com.example.ivsmirnov.keyregistrator.items.CloseRoomsParams;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 03.02.2016.
 */
public class CloseRooms extends AsyncTask<CloseRoomsParams,Integer,Integer> {

    private Context mContext;
    private RoomInterface mRoomInterface;

    public CloseRooms (Context context){
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        Values.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_thanks), Values.TOAST_POSITIVE);

    }

    @Override
    protected Integer doInBackground(CloseRoomsParams... params) {
        mRoomInterface = params[0].getRoomInterface();
        String tag = params[0].getTag();

        DataBaseJournal dataBaseJournal = new DataBaseJournal(mContext);
        DataBaseRooms dataBaseRooms = new DataBaseRooms(mContext);

        RoomItem currentRoomItem = dataBaseRooms.getRoomItemForCurrentUser(tag);
        int closedRooms = dataBaseRooms.updateRoom(currentRoomItem
                .setTag(Values.EMPTY)
                .setStatus(Values.ROOM_IS_FREE));
        dataBaseJournal.updateDB(currentRoomItem.getPositionInBase());

        dataBaseJournal.closeDB();
        dataBaseRooms.closeDB();
        return closedRooms;
    }

    @Override
    protected void onPostExecute(Integer closedRooms) {
        if (mRoomInterface!=null){
            mRoomInterface.onRoomClosed();
        }
    }
}
