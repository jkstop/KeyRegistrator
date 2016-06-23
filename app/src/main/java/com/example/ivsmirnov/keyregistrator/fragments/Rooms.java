package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterMainRoomGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

import java.util.ArrayList;

public class Rooms extends Fragment implements RecycleItemClickListener {

    public static RecyclerView mAuditroomGrid;
    private static AdapterMainRoomGrid mAdapter;
    private GridLayoutManager mGridManager;
    private Context mContext;
    private static ArrayList<RoomItem> mRoomItems;
    private static long lastClickTime = 0;

    public static Rooms newInstance (){
        return new Rooms();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_recycler,container,false);
        mContext = rootView.getContext();

        mRoomItems = new ArrayList<>();
        mAuditroomGrid = (RecyclerView)rootView.findViewById(R.id.recycler_main);

        initializeAuditroomGrid();

        return rootView;
    }


    private void initializeAuditroomGrid(){
        if (!mRoomItems.isEmpty()) mRoomItems.clear();
        mRoomItems.addAll(RoomDB.readRoomsDB());
        mAdapter = new AdapterMainRoomGrid(mContext, mRoomItems,this);
        mAuditroomGrid.setAdapter(mAdapter);
        mGridManager = new GridLayoutManager(mContext, SharedPrefs.getGridColumns());
        mAuditroomGrid.setLayoutManager(mGridManager);
    }

    public static void updateGrid(){
        if (!mRoomItems.isEmpty()) mRoomItems.clear();
        mRoomItems.addAll(RoomDB.readRoomsDB());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeAuditroomGrid();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Launcher)getActivity()).setToolbarTitle(R.string.title_rooms_loading);
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        if (mRoomItems.get(position).getStatus() == RoomDB.ROOM_IS_FREE) {
            DialogUserAuth.newInstance(mRoomItems.get(position).getAuditroom()).show(getActivity().getSupportFragmentManager(),getString(R.string.title_user_auth));
        } else {
            if (mRoomItems.get(position).getAccessType() == FavoriteDB.CLICK_USER_ACCESS) {
                ArrayList<RoomItem> items = RoomDB.getRoomItemsForCurrentUser(mRoomItems.get(position).getTag());
                if (items.size()!=0 && items.size() > 1){
                    DialogCloseRoomSelection.newInstance(mRoomItems.get(position).getTag()).show(getChildFragmentManager(), getString(R.string.title_dialog_close_room_choise));
                } else if (items.size() == 1){
                    new BaseWriter(BaseWriter.UPDATE_CURRENT, (BaseWriter.Callback)getActivity())
                            .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new BaseWriterParams()
                                    .setPersonTag(mRoomItems.get(position).getTag())
                                    .setOpenTime(mRoomItems.get(position).getTime()));
                }
            }else{
                Toasts.handler.sendEmptyMessage(Toasts.TOAST_PUT_CARD_FIRST);
            }
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        if (mRoomItems.get(position).getStatus() == RoomDB.ROOM_IS_BUSY){
            if (mRoomItems.get(position).getAccessType()== FavoriteDB.CARD_USER_ACCESS){
                DialogPassword.newInstance(mRoomItems.get(position).getTag(), null)
                        .show(getFragmentManager(), DialogPassword.ROOMS_ACCESS);
            }
        }
    }
}
