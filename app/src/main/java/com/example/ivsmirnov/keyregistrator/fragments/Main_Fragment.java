package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_main_auditrooms_grid;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

public class Main_Fragment extends Fragment implements UpdateMainFrame,RecycleItemClickListener{


    public static RecyclerView mAuditroomGrid;

    private SharedPreferences.Editor preferencesEditor;
    private SharedPreferences preferences;
    private Context mContext;

    private ArrayList<RoomItem> mRoomItems;

    private LinearLayout disclaimer;
    private FrameLayout frameForGrid;

    adapter_main_auditrooms_grid mAuditroomGridAdapter;

    private static long lastClickTime = 0;

    public static Main_Fragment newInstance (){
        return new Main_Fragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_main_fragment,container,false);
        mContext = rootView.getContext();

        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mRoomItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        frameForGrid = (FrameLayout) rootView.findViewById(R.id.frame_for_grid_aud);

        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        mAuditroomGrid = (RecyclerView)rootView.findViewById(R.id.main_fragment_auditroom_grid);
        mAuditroomGrid.setHasFixedSize(true);

        initializeAuditroomGrid();

        TextView textEmptyAud = (TextView)rootView.findViewById(R.id.text_empty_aud_list);
        if (mRoomItems.isEmpty()){
            textEmptyAud.setVisibility(View.VISIBLE);
        }else{
            textEmptyAud.setVisibility(View.INVISIBLE);
        }
        float disclaimer_size = preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15);
        disclaimer = (LinearLayout)rootView.findViewById(R.id.disclaimer);
        disclaimer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, disclaimer_size));
        return rootView;
    }

    private void initializeAuditroomGrid(){
        final int columns = preferences.getInt(Values.COLUMNS_AUD_COUNT, 1);

        mAuditroomGridAdapter = new adapter_main_auditrooms_grid(mContext,mRoomItems,this);
        mAuditroomGrid.setAdapter(mAuditroomGridAdapter);
        mAuditroomGrid.setLayoutManager(new GridLayoutManager(mContext,columns));
    }


    @Override
    public void onResume() {
        super.onResume();

        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mRoomItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        float grid_weight = preferences.getFloat(Values.GRID_SIZE, (float) 0.45);
        frameForGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, grid_weight));

        initializeAuditroomGrid();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_items_size:
                Dialog_Fragment dialog_grid_size = new Dialog_Fragment();
                Bundle bundle_grid = new Bundle();
                bundle_grid.putInt(Values.DIALOG_TYPE, Values.DIALOG_SEEKBARS);
                dialog_grid_size.setArguments(bundle_grid);
                dialog_grid_size.setTargetFragment(this, 0);
                dialog_grid_size.show(getFragmentManager(), "seek_grid_size");
                return true;
            case R.id.test:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if (resultCode==Activity.RESULT_OK){
                Uri uri = data.getData();
                String path = uri.getPath();
                Log.d("result",path);
            }else {
                Log.d("result","canceled");
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher) getActivity()).getSupportActionBar() != null) {
            ((Launcher) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_main));
            Log.d("toolbar", String.valueOf(((Launcher) getActivity()).getSupportActionBar().getHeight()));
        }
    }
*/
    @Override
    public void onFinish() {
        float disclaimer_size = preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15);
        float grid_weight = preferences.getFloat(Values.GRID_SIZE, (float) 0.45);

        disclaimer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, disclaimer_size));
        frameForGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, grid_weight));
    }

    @Override
    public void onItemClick(View v, int position) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        if (mRoomItems.get(position).Status==1) {
            Bundle bundle = new Bundle();
            bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
            bundle.putString(Values.AUDITROOM, mRoomItems.get(position).Auditroom);
            Nfc_Fragment nfc_fragment = Nfc_Fragment.newInstance();
            nfc_fragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, nfc_fragment, getResources().getString(R.string.fragment_tag_nfc)).commit();
        } else {
            if (mRoomItems.get(position).Access==0) {

                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                dbJournal.updateDB(mRoomItems.get(position).PositionInBase);
                dbJournal.closeDB();

                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                dbRooms.updateRoom(new RoomItem(mRoomItems.get(position).Auditroom,
                        Values.ROOM_IS_FREE,
                        Values.ACCESS_BY_CLICK,
                        0,
                        null,
                        null,
                        null));
                dbRooms.closeDB();
                getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.fragment_tag_main)).commit();
            }else{
                Values.showFullscreenToast(mContext,getResources().getString(R.string.text_toast_put_card));
            }
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        if (mRoomItems.get(position).Access==1){
            Dialog_Fragment dialog_fragment = new Dialog_Fragment();
            Bundle bundle = new Bundle();
            bundle.putLong(Values.POSITION_IN_BASE_FOR_ROOM,mRoomItems.get(position).PositionInBase);
            bundle.putString("aud",mRoomItems.get(position).Auditroom);
            bundle.putInt(Values.DIALOG_CLOSE_ROOM_TYPE,Values.DIALOG_CLOSE_ROOM_TYPE_ROOMS);
            bundle.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLOSE_ROOM);
            dialog_fragment.setArguments(bundle);
            dialog_fragment.show(getFragmentManager(),"enter_pin");
        }
    }
}
