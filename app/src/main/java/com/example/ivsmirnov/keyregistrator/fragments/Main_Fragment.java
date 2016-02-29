package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.RoomInterface;
import com.example.ivsmirnov.keyregistrator.items.CloseRoomsParams;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Main_Fragment extends Fragment implements UpdateInterface,RecycleItemClickListener, Get_Account_Information_Interface, RoomInterface{


    public static RecyclerView mAuditroomGrid;

    private Context mContext;
    private Settings mSettings;

    private DataBaseFavorite mDataBaseFavorite;
    private DataBaseRooms mDataBaseRooms;

    private ArrayList<RoomItem> mRoomItems;

    private LinearLayout disclaimer;
    private FrameLayout frameForGrid;

    private CardView mDisclaimerCard;

    adapter_main_auditrooms_grid mAuditroomGridAdapter;

    public static RoomInterface roomInterface;

    private static long lastClickTime = 0;


    public static Main_Fragment newInstance (){
        return new Main_Fragment();
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
        View rootView = inflater.inflate(R.layout.layout_main_fragment,container,false);
        mContext = rootView.getContext();
        mDataBaseFavorite = Launcher.mDataBaseFavorite;

        if (Launcher.mDataBaseRooms!=null){
            mDataBaseRooms = Launcher.mDataBaseRooms;
        } else {
            mDataBaseRooms = new DataBaseRooms(mContext);
        }
        mRoomItems = mDataBaseRooms.readRoomsDB();


        roomInterface = this;

        frameForGrid = (FrameLayout) rootView.findViewById(R.id.frame_for_grid_aud);

        mSettings = new Settings(mContext);

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
        int weightCard = mSettings.getDisclaimerWeight();
        ((LinearLayout.LayoutParams) mDisclaimerCard.getLayoutParams()).weight = weightCard;
        ((LinearLayout.LayoutParams) frameForGrid.getLayoutParams()).weight = 100 - weightCard;
    }

    private void initializeAuditroomGrid(){
        mAuditroomGridAdapter = new adapter_main_auditrooms_grid(mContext,mRoomItems,this);
        mAuditroomGrid.setAdapter(mAuditroomGridAdapter);
        mAuditroomGrid.setLayoutManager(new GridLayoutManager(mContext,mSettings.getAuditroomColumnsCount()));
    }


    @Override
    public void onResume() {
        super.onResume();

        if (Launcher.mDataBaseRooms!=null){
            mDataBaseRooms = Launcher.mDataBaseRooms;
        } else {
            mDataBaseRooms = new DataBaseRooms(mContext);
        }
        mRoomItems = mDataBaseRooms.readRoomsDB();


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
                Dialog_Fragment dialog_resize = new Dialog_Fragment();
                Bundle bundle_dialog_resize = new Bundle();
                bundle_dialog_resize.putInt(Values.DIALOG_TYPE, Values.DIALOG_RESIZE_ITEMS);
                dialog_resize.setArguments(bundle_dialog_resize);
                dialog_resize.setTargetFragment(this,0);
                dialog_resize.show(getFragmentManager(),"dialog_resize");
                return true;
            case R.id.test:
                new insert(mContext).execute();
                return true;
            //case R.id.test2:
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class insert extends AsyncTask<Void,Integer,Void>{

        private Context context;
        private ProgressDialog progressDialog;

        private insert(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            for (int i=0;i<1000;i++){
                /*PersonItem personItem = mDataBaseFavorite.getPersonItem("99 77 DC 1A 00 00",DataBaseFavorite.SERVER_USER,DataBaseFavorite.FULLSIZE_PHOTO);
                mDataBaseFavorite.writeInDBTeachers(personItem
                        .setRadioLabel(String.valueOf(new Random().nextLong() % (100000 - 1)) + 1)
                        .setPhotoPreview(DataBaseFavorite.getPhotoPreview(personItem.getPhotoOriginal())));*/
                long timeIN = System.currentTimeMillis();
                Launcher.mDataBaseJournal.writeInDBJournal(new JournalItem().setAccessType(DataBaseJournal.ACCESS_BY_CARD)
                        .setAccountID(mSettings.getActiveAccountID())
                        .setAuditroom("111")
                        .setPersonLastname("testUser")
                        .setPersonFirstname("testUser")
                        .setTimeIn(timeIN)
                        .setTimeOut(System.currentTimeMillis()+100)
                        .setPersonPhoto(DataBaseFavorite.getBase64DefaultPhotoFromResources(mContext,"М")));
                publishProgress(i);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setMessage(String.valueOf(values[0]));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.cancel();
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
        if (mRoomItems.get(position).getStatus()==Values.ROOM_IS_FREE) {
            mSettings.setLastClickedAuditroom(mRoomItems.get(position).getAuditroom());
            Bundle bundle = new Bundle();
            bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
            Nfc_Fragment nfc_fragment = Nfc_Fragment.newInstance();
            nfc_fragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, nfc_fragment, getResources().getString(R.string.fragment_tag_nfc)).commit();
        } else {
            if (mRoomItems.get(position).getAccessType()==DataBaseJournal.ACCESS_BY_CLICK) {
                new CloseRooms(mContext).execute(new CloseRoomsParams()
                        .setTag(mRoomItems.get(position).getTag())
                        .setRoomInterface(roomInterface));
            }else{
                Values.showFullscreenToast(mContext,getResources().getString(R.string.text_toast_put_card),Values.TOAST_NEGATIVE);
            }
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        if (mRoomItems.get(position).getAccessType()==DataBaseJournal.ACCESS_BY_CARD){
            Dialog_Fragment dialog_fragment = new Dialog_Fragment();
            Bundle bundle = new Bundle();
            bundle.putLong(Values.POSITION_IN_BASE_FOR_ROOM,mRoomItems.get(position).getPositionInBase());
            bundle.putString("aud",mRoomItems.get(position).getAuditroom());
            bundle.putString("tag",mRoomItems.get(position).getTag());
            bundle.putLong("positionInBase",mRoomItems.get(position).getPositionInBase());
            bundle.putInt(Values.DIALOG_CLOSE_ROOM_TYPE,Values.DIALOG_CLOSE_ROOM_TYPE_ROOMS);
            bundle.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLOSE_ROOM);
            dialog_fragment.setArguments(bundle);
            dialog_fragment.show(getFragmentManager(),"enter_pin");
        }
    }

    @Override
    public void updateInformation() {
        setLayoutsWeight();
        initializeAuditroomGrid();
    }


    @Override
    public void onUserRecoverableAuthException(UserRecoverableAuthException e) {
        startActivityForResult(e.getIntent(),123);
    }

    @Override
    public void onChangeAccount() {
    }

    @Override
    public void onAccountImageLoaded(Bitmap bitmap) {

    }

    @Override
    public void onRoomClosed() {
        getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.fragment_tag_main)).commit();
    }

}
