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
import com.example.ivsmirnov.keyregistrator.adapters.adapter_main_auditrooms_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

public class Rooms_Fragment extends Fragment implements UpdateInterface, RecycleItemClickListener {


    private ArrayList<RoomItem> mRoomItems;
    private Context mContext;
    private adapter_main_auditrooms_grid mAdapter;
    private FloatingActionButton mAddFAB;

    private RecyclerView mRoomsGrid;

    private Settings mSettings;

    //private SharedPreferences sharedPreferences;
    //private SharedPreferences.Editor editor;

    public static Rooms_Fragment newInstance(){
        return new Rooms_Fragment();
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
        View rootView = inflater.inflate(R.layout.layout_auditrooms_fragment,container,false);
        mContext = rootView.getContext();
        mSettings = new Settings(mContext);

        mRoomsGrid = (RecyclerView)rootView.findViewById(R.id.auditroom_fragment_room_grid);
        mRoomsGrid.setHasFixedSize(true);

        mAddFAB = (FloatingActionButton)rootView.findViewById(R.id.auditroom_fragment_fab);

        mAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle b = new Bundle();
                b.putInt(Values.DIALOG_TYPE, Values.ADD_ROOM_DIALOG);
                dialog.setArguments(b);
                dialog.setTargetFragment(Rooms_Fragment.this, 0);
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
        mRoomsGrid.setLayoutManager(new GridLayoutManager(mContext, mSettings.getAuditroomColumnsCount()));

        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mRoomItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        for (int i=0;i<mRoomItems.size();i++){
            mRoomItems.get(i).setStatus(Values.ROOM_IS_FREE);
        }

        mAdapter = new adapter_main_auditrooms_grid(mContext,mRoomItems,this);
        mRoomsGrid.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_auditrooms_set_columns_number:
                Dialog_Fragment dialog_fragment = new Dialog_Fragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Values.DIALOG_TYPE, Values.SELECT_COLUMNS_DIALOG);
                bundle.putString("AudOrPer", "aud");
                dialog_fragment.setArguments(bundle);
                dialog_fragment.setTargetFragment(Rooms_Fragment.this, 0);
                dialog_fragment.show(getFragmentManager(),"columns");
                return true;
            case R.id.menu_auditrooms_save_to_file:
                Save_to_file saveToFile = new Save_to_file(mContext,Values.WRITE_ROOMS, true);
                saveToFile.execute();
                return true;
            case R.id.menu_auditrooms_load_from_file:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Values.REQUEST_CODE_LOAD_ROOMS);
                return true;
            case R.id.menu_auditrooms_clear:
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle bundleRooms = new Bundle();
                bundleRooms.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLEAR_ROOMS);
                dialog.setArguments(bundleRooms);
                dialog.setTargetFragment(Rooms_Fragment.this,0);
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
            if (requestCode == Values.REQUEST_CODE_LOAD_ROOMS){
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    Loader_intent loader_intent = new Loader_intent(mContext,path,this,Values.REQUEST_CODE_LOAD_ROOMS);
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
    public void onItemClick(View v, int position) {
        Dialog_Fragment dialog = new Dialog_Fragment();
        Bundle b = new Bundle();
        b.putString("aud", mRoomItems.get(position).getAuditroom());
        b.putInt(Values.DIALOG_TYPE,Values.DELETE_ROOM_DIALOG);
        dialog.setArguments(b);
        dialog.setTargetFragment(Rooms_Fragment.this,0);
        dialog.show(getFragmentManager(),"delete_room");
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {

    }
}
