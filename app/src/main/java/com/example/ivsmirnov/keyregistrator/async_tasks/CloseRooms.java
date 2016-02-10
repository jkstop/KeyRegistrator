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
    private ProgressDialog mProgressDialog;

    public CloseRooms (Context context){
        this.mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        Values.showFullscreenToast(mContext, mContext.getString(R.string.text_toast_thanks), Values.TOAST_POSITIVE);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Подождите...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(CloseRoomsParams... params) {
        mRoomInterface = params[0].getRoomInterface();
        int closedRooms = 0;

        DataBaseJournal dataBaseJournal = new DataBaseJournal(mContext);
        DataBaseRooms dataBaseRooms = new DataBaseRooms(mContext);
        ArrayList<RoomItem> roomItemArrayList = dataBaseRooms.readRoomsDB();
        for (RoomItem roomItem : roomItemArrayList){
            if (roomItem.Tag!=null && roomItem.Tag.equals(params[0].getTag())){
                dataBaseJournal.updateDB(roomItem.PositionInBase);
                roomItem.Status = Values.ROOM_IS_FREE;
                roomItem.Tag = Values.EMPTY;
                dataBaseRooms.updateRoom(roomItem);
                closedRooms++;
            }
        }
        dataBaseJournal.closeDB();
        dataBaseRooms.closeDB();
        return closedRooms;
    }

    @Override
    protected void onPostExecute(Integer closedRooms) {
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }

        if (mRoomInterface!=null){
            mRoomInterface.onRoomClosed(closedRooms);
        }
    }
}
