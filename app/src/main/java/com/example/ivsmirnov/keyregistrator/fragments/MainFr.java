package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.activities.UserAuth;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterMainRoomGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

import java.util.ArrayList;

public class MainFr extends Fragment implements RecycleItemClickListener {

    public static RecyclerView mAuditroomGrid;
    private int mCurrentOrientation;
    private int mGridHeight = 0;

    private int roomGridH = 0;

    public static AdapterMainRoomGrid mAdapter;
    private GridLayoutManager mGridManager;

    private Context mContext;
    public static ArrayList<RoomItem> mRoomItems;
    //private FrameLayout mFrameForGrid;
    //private CardView mDisclaimerCard;

    private static long lastClickTime = 0;

    public static MainFr newInstance (){
        return new MainFr();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //setRetainInstance(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        mGridManager.setSpanCount(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? Settings.getColumnsLandscape() : Settings.getColumnsPortrait());

        for (RoomItem roomItem : mRoomItems){
            roomItem.setGridHeight(mAuditroomGrid.getWidth());
            roomItem.setGridOrient(newConfig.orientation);
        }
        mAdapter.notifyDataSetChanged();

        super.onConfigurationChanged(newConfig);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_recycler,container,false);
        mContext = rootView.getContext();

        mRoomItems = new ArrayList<>();

        mCurrentOrientation = getResources().getConfiguration().orientation;

        //mCloseRoomInterface = (CloseRoomInterface)getActivity();

        //mFrameForGrid = (FrameLayout) rootView.findViewById(R.id.frame_for_grid_aud);

        mAuditroomGrid = (RecyclerView)rootView.findViewById(R.id.recycler_main);
        //mAuditroomGrid.setHasFixedSize(true);

        initializeAuditroomGrid();

        //TextView textEmptyAud = (TextView)rootView.findViewById(R.id.text_empty_aud_list);

        //if (mRoomItems.isEmpty()){
        //    textEmptyAud.setVisibility(View.VISIBLE);
        //}

        //mDisclaimerCard = (CardView)rootView.findViewById(R.id.layout_main_fragment_disclaimer_card);

        //setLayoutsWeight();
        return rootView;
    }


    private void initializeAuditroomGrid(){
        if (!mRoomItems.isEmpty()) mRoomItems.clear();
        mRoomItems.addAll(RoomDB.readRoomsDB());

        mAdapter = new AdapterMainRoomGrid(mContext, mRoomItems,this);
        mAuditroomGrid.setAdapter(mAdapter);
        mGridManager = new GridLayoutManager(mContext,2);
        switch (getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                mGridManager.setSpanCount(Settings.getColumnsLandscape());
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                mGridManager.setSpanCount(Settings.getColumnsPortrait());
                break;
            default:
                break;
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main,menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Launcher)getActivity()).setToolbarTitle(R.string.toolbar_title_main);
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        if (mRoomItems.get(position).getStatus() == RoomDB.ROOM_IS_FREE) {
            //startActivity(new Intent(mContext, UserAuth.class).putExtra(PersonsFr.PERSONS_SELECTED_ROOM, mRoomItems.get(position).getAuditroom()));
            DialogUserAuth.newInstance(mRoomItems.get(position).getAuditroom()).show(getActivity().getSupportFragmentManager(),getString(R.string.title_activity_user_auth));
        } else {
            if (mRoomItems.get(position).getAccessType() == FavoriteDB.CLICK_USER_ACCESS) {

                new BaseWriter(BaseWriter.UPDATE_CURRENT, mContext, (BaseWriter.Callback)getActivity())
                        .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new BaseWriterParams().setPersonTag(mRoomItems.get(position).getTag()));
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
