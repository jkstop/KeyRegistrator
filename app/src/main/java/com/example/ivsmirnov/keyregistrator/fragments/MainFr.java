package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.example.ivsmirnov.keyregistrator.adapters.AdapterMainRoomGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
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

    private Context mContext;
    private ArrayList<RoomItem> mRoomItems;
    private FrameLayout mFrameForGrid;
    private CardView mDisclaimerCard;
    private CloseRoomInterface mCloseRoomInterface;

    private static long lastClickTime = 0;

    public static MainFr newInstance (){
        return new MainFr();
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
        View rootView = inflater.inflate(R.layout.layout_main_fr,container,false);
        mContext = rootView.getContext();


        mRoomItems = RoomDB.readRoomsDB();

        mCloseRoomInterface = (CloseRoomInterface)getActivity();

        mFrameForGrid = (FrameLayout) rootView.findViewById(R.id.frame_for_grid_aud);

        mAuditroomGrid = (RecyclerView)rootView.findViewById(R.id.main_fragment_auditroom_grid);
        mAuditroomGrid.setHasFixedSize(true);

        initializeAuditroomGrid();

        TextView textEmptyAud = (TextView)rootView.findViewById(R.id.text_empty_aud_list);
        if (mRoomItems.isEmpty()){
            textEmptyAud.setVisibility(View.VISIBLE);
        }

        mDisclaimerCard = (CardView)rootView.findViewById(R.id.layout_main_fragment_disclaimer_card);

        setLayoutsWeight();
        return rootView;
    }

    private void setLayoutsWeight(){
        int weightCard = Settings.getDisclaimerWeight();
        ((LinearLayout.LayoutParams) mDisclaimerCard.getLayoutParams()).weight = weightCard;
        ((LinearLayout.LayoutParams) mFrameForGrid.getLayoutParams()).weight = 100 - weightCard;
    }

    private void initializeAuditroomGrid(){
        AdapterMainRoomGrid mAuditroomGridAdapter = new AdapterMainRoomGrid(mContext, mRoomItems,this);
        mAuditroomGrid.setAdapter(mAuditroomGridAdapter);

        //Layout manager для сетки с пользователями, отключение прокрутки
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(mContext, Settings.getAuditroomColumnsCount()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        mAuditroomGrid.setLayoutManager(mGridLayoutManager);
    }


    @Override
    public void onResume() {
        super.onResume();

        mRoomItems = RoomDB.readRoomsDB();

        setLayoutsWeight();

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
                Dialogs dialog_resize = new Dialogs();
                Bundle bundle_dialog_resize = new Bundle();
                bundle_dialog_resize.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_RESIZE_ITEMS);
                dialog_resize.setArguments(bundle_dialog_resize);
                dialog_resize.setTargetFragment(this,0);
                dialog_resize.show(getFragmentManager(),"dialog_resize");
                return true;
            //case R.id.test:
                //new insert(mContext).execute();
              //  return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.toolbar_title_main));
        }
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        if (mRoomItems.get(position).getStatus() == RoomDB.ROOM_IS_FREE) {
            Settings.setLastClickedAuditroom(mRoomItems.get(position).getAuditroom());
            Bundle bundle = new Bundle();
            bundle.putInt(PersonsFr.PERSONS_FRAGMENT_TYPE, PersonsFr.PERSONS_FRAGMENT_SELECTOR);
            UserAuthFr nfc_fr = UserAuthFr.newInstance();
            nfc_fr.setArguments(bundle);

            Launcher.showFragment(getActivity().getSupportFragmentManager(), nfc_fr, R.string.fragment_tag_nfc);

            if (Launcher.sCardConnected && Launcher.sReaderStateChangeListener!=null) Launcher.sReaderStateChangeListener.onStateChange(0, 1, 2);


        } else {
            if (mRoomItems.get(position).getAccessType() == JournalDB.ACCESS_BY_CLICK) {
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
            if (mRoomItems.get(position).getAccessType()== JournalDB.ACCESS_BY_CARD){
                Dialogs dialogs = new Dialogs();
                Bundle bundle = new Bundle();
                //bundle.putLong(Values.POSITION_IN_BASE_FOR_ROOM,mRoomItems.get(position).getTime());
                bundle.putString("aud",mRoomItems.get(position).getAuditroom());
                bundle.putString("tag",mRoomItems.get(position).getTag());
                //bundle.putLong("positionInBase",mRoomItems.get(position).getTime());
                bundle.putInt(Dialogs.DIALOG_ENTER_PASSWORD_TYPE, Dialogs.DIALOG_ENTER_PASSWORD_TYPE_CLOSE_ROOM);
                bundle.putInt(Dialogs.DIALOG_TYPE,Dialogs.DIALOG_ENTER_PASSWORD);
                dialogs.setArguments(bundle);
                dialogs.show(getFragmentManager(),"enter_pin");
            }
        }
    }

    @Override
    public void updateInformation() {
        setLayoutsWeight();
        initializeAuditroomGrid();
    }

}
