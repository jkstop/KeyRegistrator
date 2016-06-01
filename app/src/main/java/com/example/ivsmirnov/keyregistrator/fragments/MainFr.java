package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.services.Toasts;

import java.util.ArrayList;

public class MainFr extends Fragment implements UpdateInterface,RecycleItemClickListener {

    public static RecyclerView mAuditroomGrid;
    private int mCurrentOrientation;
    private int mGridHeight = 0;

    private int roomGridH = 0;

    private AdapterMainRoomGrid mAdapter;
    private GridLayoutManager mGridManager;

    private Context mContext;
    private ArrayList<RoomItem> mRoomItems;
    //private FrameLayout mFrameForGrid;
    //private CardView mDisclaimerCard;
    private CloseRoomInterface mCloseRoomInterface;

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

        mCloseRoomInterface = (CloseRoomInterface)getActivity();

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
        mAdapter.hasStableIds();
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


    @Override
    public void onResume() {
        super.onResume();

        //mRoomItems = RoomDB.readRoomsDB();

        //setLayoutsWeight();

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

        //System.out.println("init mainFr main_content 1");
        //ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        //if (actionBar != null) {
            //System.out.println("init mainFr main_content 2");
            //actionBar.setTitle(getResources().getString(R.string.toolbar_title_main));
            //actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setDisplayShowHomeEnabled(true);
            //actionBar.setHomeButtonEnabled(true);
        //}
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        if (mRoomItems.get(position).getStatus() == RoomDB.ROOM_IS_FREE) {
            Settings.setLastClickedAuditroom(mRoomItems.get(position).getAuditroom());
            //Bundle bundle = new Bundle();
            //bundle.putInt(PersonsFr.PERSONS_FRAGMENT_TYPE, PersonsFr.PERSONS_FRAGMENT_SELECTOR);
            //UserAuthFr nfc_fr = UserAuthFr.newInstance();
            //nfc_fr.setArguments(bundle);

            //Launcher.showFragment(getActivity().getSupportFragmentManager(), nfc_fr, R.string.fragment_tag_nfc);

            //if (Launcher.sCardConnected && Launcher.sReaderStateChangeListener!=null) Launcher.sReaderStateChangeListener.onStateChange(0, 1, 2);

            //new DialogUserAuth().show(getFragmentManager(),);

            startActivity(new Intent(mContext, UserAuth.class).putExtra(PersonsFr.PERSONS_SELECTED_ROOM, mRoomItems.get(position).getAuditroom()));


        } else {
            if (mRoomItems.get(position).getAccessType() == FavoriteDB.CLICK_USER_ACCESS) {
                new CloseRooms(mContext, mRoomItems.get(position).getTag(), mCloseRoomInterface).execute();
            }else{
                Toasts.showFullscreenToast(mContext,getResources().getString(R.string.text_toast_put_card),Toasts.TOAST_NEGATIVE);
            }
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        if (mRoomItems.get(position).getStatus() == RoomDB.ROOM_IS_BUSY){
            if (mRoomItems.get(position).getAccessType()== FavoriteDB.CARD_USER_ACCESS){
                //Dialogs dialogs = new Dialogs();
                //Bundle bundle = new Bundle();
                //bundle.putLong(Values.POSITION_IN_BASE_FOR_ROOM,mRoomItems.get(position).getTime());
                //bundle.putString("aud",mRoomItems.get(position).getAuditroom());
                //bundle.putString("tag",mRoomItems.get(position).getTag());
                //bundle.putLong("positionInBase",mRoomItems.get(position).getTime());
                //bundle.putInt(Dialogs.DIALOG_ENTER_PASSWORD_TYPE, Dialogs.DIALOG_ENTER_PASSWORD_TYPE_CLOSE_ROOM);
                //bundle.putInt(Dialogs.DIALOG_TYPE,Dialogs.DIALOG_ENTER_PASSWORD);
                //dialogs.setArguments(bundle);
                //dialogs.show(getFragmentManager(),"enter_pin");
                DialogPassword.newInstance(mRoomItems.get(position).getTag())
                        .show(getFragmentManager(), DialogPassword.ROOMS_ACCESS);
            }
        }
    }

    @Override
    public void updateInformation() {
        //setLayoutsWeight();
        initializeAuditroomGrid();
    }

}
