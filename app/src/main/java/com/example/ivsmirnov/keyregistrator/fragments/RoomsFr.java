package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterMainRoomGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileLoader;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

public class RoomsFr extends Fragment implements UpdateInterface, RecycleItemClickListener {


    private ArrayList<RoomItem> mRoomItems;
    private Context mContext;


    private AdapterMainRoomGrid mAdapter;
    private GridLayoutManager mGridManager;
    private static RecyclerView mRoomsGrid;

    public static RoomsFr newInstance(){
        return new RoomsFr();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_recycler,container,false);
        mContext = rootView.getContext();

        mRoomsGrid = (RecyclerView)rootView.findViewById(R.id.recycler_main);
        mRoomsGrid.setHasFixedSize(true);

        //FloatingActionButton mAddFAB = (FloatingActionButton) rootView.findViewById(R.id.auditroom_fragment_fab);
/*
        mAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs dialog = new Dialogs();
                Bundle b = new Bundle();
                b.putInt(Dialogs.DIALOG_TYPE, Dialogs.ADD_ROOM_DIALOG);
                dialog.setArguments(b);
                dialog.setTargetFragment(RoomsFr.this, 0);
                dialog.show(getFragmentManager(), "add_room");
            }
        });
*/
        initializeAuditroomsGrid();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Launcher)getActivity()).setToolbarTitle(R.string.toolbar_title_auditrooms);
        //ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        //if (actionBar != null) {
         //   actionBar.setTitle(getResources().getString(R.string.toolbar_title_auditrooms));
        //}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mGridManager.setSpanCount(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? Settings.getColumnsLandscape() : Settings.getColumnsPortrait());

        for (RoomItem roomItem : mRoomItems){
            roomItem.setGridHeight(mRoomsGrid.getWidth());
            roomItem.setGridOrient(newConfig.orientation);
        }
        mAdapter.notifyDataSetChanged();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeAuditroomsGrid();
    }

    private void initializeAuditroomsGrid(){
        mRoomsGrid.setLayoutManager(new GridLayoutManager(mContext, Settings.getAuditroomColumnsCount()));

        mRoomItems = RoomDB.readRoomsDB();

        for (int i=0;i<mRoomItems.size();i++){
            mRoomItems.get(i).setStatus(RoomDB.ROOM_IS_FREE);
        }

        mAdapter = new AdapterMainRoomGrid(mContext, mRoomItems,this);
        mAdapter.hasStableIds();
        mRoomsGrid.setAdapter(mAdapter);

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
        mRoomsGrid.setLayoutManager(mGridManager);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (requestCode == FileLoader.REQUEST_CODE_LOAD_ROOMS){
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    FileLoader fileLoader_ = new FileLoader(mContext,path);
                    fileLoader_.execute();
                }
            }
        }
    }

    @Override
    public void updateInformation() {
        initializeAuditroomsGrid();
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        Dialogs dialog = new Dialogs();
        Bundle b = new Bundle();
        b.putString("aud", mRoomItems.get(position).getAuditroom());
        b.putInt(Dialogs.DIALOG_TYPE, Dialogs.DELETE_ROOM_DIALOG);
        dialog.setArguments(b);
        dialog.setTargetFragment(RoomsFr.this,0);
        dialog.show(getFragmentManager(),"delete_room");
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {

    }
}
