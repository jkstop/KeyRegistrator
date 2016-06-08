package com.example.ivsmirnov.keyregistrator.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterEmailExtras;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Илья on 07.06.2016.
 */
public class RoomsPreference extends DialogPreference implements AdapterEmailExtras.Callback{

    private Context mContext;
    private ArrayList<String> mRoomList;
    private ArrayList<String> mPreviousRoomList;
    private AdapterEmailExtras mAdapter;
    private int pressedButton = 0;

    public RoomsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton("Добавить", null);
    }

    @Override
    protected View onCreateDialogView() {
        View dialogView = View.inflate(mContext, R.layout.view_email_extra_list, null);
        mRoomList = new ArrayList<>();
        mRoomList.addAll(RoomDB.getRoomList());
        mPreviousRoomList = new ArrayList<>(mRoomList);
        RecyclerView roomListRecycler = (RecyclerView)dialogView.findViewById(R.id.preference_email_extra_list);
        mAdapter = new AdapterEmailExtras(mContext, AdapterEmailExtras.ROOMS, mRoomList, this);
        roomListRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        roomListRecycler.setAdapter(mAdapter);
        return dialogView;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog dialog = (AlertDialog)getDialog();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //focus - http://stackoverflow.com/questions/9102074/android-edittext-in-dialog-doesnt-pull-up-soft-keyboard
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRoomList.contains(AdapterEmailExtras.ADD_NEW_ITEM)){
                    mRoomList.add(AdapterEmailExtras.ADD_NEW_ITEM);
                    mAdapter.notifyItemInserted(mRoomList.size());
                }
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        pressedButton = which;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (pressedButton == AlertDialog.BUTTON_POSITIVE){
            if (mRoomList.contains(AdapterEmailExtras.ADD_NEW_ITEM)){
                mRoomList.remove(AdapterEmailExtras.ADD_NEW_ITEM);
            }

            for (String s : mRoomList){
                if (!mPreviousRoomList.contains(s)){ //такого помещения не было, пишем
                    RoomDB.writeInRoomsDB(new RoomItem()
                            .setAuditroom(s)
                            .setStatus(RoomDB.ROOM_IS_FREE)
                            .setAccessType(FavoriteDB.CLICK_USER_ACCESS));
                }
            }

            for (String s : mPreviousRoomList){
                if (!mRoomList.contains(s)){ //помещение было, но его удалили
                    RoomDB.deleteFromRoomsDB(s);
                }
            }

            pressedButton = 0;
        }

    }

    @Override
    public void onDeleteItem(int position) {
        mRoomList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onAddItem(String item) {
        mRoomList.add(item);
        mRoomList.remove(AdapterEmailExtras.ADD_NEW_ITEM);

        Collections.sort(mRoomList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        mAdapter.notifyDataSetChanged();
    }
}
