package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.FileWriter;
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
    private FloatingActionButton mAddFAB;

    private RecyclerView mRoomsGrid;

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
        View rootView = inflater.inflate(R.layout.layout_auditrooms_fr,container,false);
        mContext = rootView.getContext();

        mRoomsGrid = (RecyclerView)rootView.findViewById(R.id.auditroom_fragment_room_grid);
        mRoomsGrid.setHasFixedSize(true);

        mAddFAB = (FloatingActionButton)rootView.findViewById(R.id.auditroom_fragment_fab);

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

        initializeAuditroomsGrid();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.toolbar_title_auditrooms));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_auditrooms, menu);
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

        mAdapter = new AdapterMainRoomGrid(mRoomItems,this);
        mRoomsGrid.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_auditrooms_set_columns_number:
                Dialogs dialogs = new Dialogs();
                Bundle bundle = new Bundle();
                bundle.putInt(Dialogs.DIALOG_TYPE, Dialogs.SELECT_COLUMNS_DIALOG);
                dialogs.setArguments(bundle);
                dialogs.setTargetFragment(RoomsFr.this, 0);
                dialogs.show(getFragmentManager(),"columns");
                return true;
            case R.id.menu_auditrooms_save_to_file:
                FileWriter saveToFile = new FileWriter(mContext, FileWriter.WRITE_ROOMS, true);
                saveToFile.execute();
                return true;
            case R.id.menu_auditrooms_load_from_file:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Loader_intent.REQUEST_CODE_LOAD_ROOMS);
                return true;
            case R.id.menu_auditrooms_clear:
                Dialogs dialog = new Dialogs();
                Bundle bundleRooms = new Bundle();
                bundleRooms.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_CLEAR_ROOMS);
                dialog.setArguments(bundleRooms);
                dialog.setTargetFragment(RoomsFr.this,0);
                dialog.show(getFragmentManager(),"clearRooms");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (requestCode == Loader_intent.REQUEST_CODE_LOAD_ROOMS){
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    Loader_intent loader_intent = new Loader_intent(mContext,path,this,Loader_intent.REQUEST_CODE_LOAD_ROOMS);
                    loader_intent.execute();
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
