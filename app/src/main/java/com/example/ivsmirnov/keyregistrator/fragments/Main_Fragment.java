package com.example.ivsmirnov.keyregistrator.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.Get_Account_Information;
import com.example.ivsmirnov.keyregistrator.interfaces.RoomInterface;
import com.example.ivsmirnov.keyregistrator.items.CloseRoomsParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main_Fragment extends Fragment implements UpdateInterface,RecycleItemClickListener, Get_Account_Information_Interface, RoomInterface{



    public static RecyclerView mAuditroomGrid;

    private Context mContext;
    private Settings mSettings;

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

        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mRoomItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();


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

        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mRoomItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

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


                return true;
            case R.id.test2:

                return true;
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == 123){
            if (resultCode == Activity.RESULT_OK){
                try {
                    Get_Account_Information get_account_information = new Get_Account_Information(mContext,data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME),this);
                    get_account_information.execute();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 1111){
            if (resultCode == Activity.RESULT_OK){
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    // Get account information
                    Log.d("name",acct.getDisplayName());
                    Log.d("email",acct.getEmail());
                }
            }
        }
    }

/*
    private String getUserName(final String token){

        final String[] lastname = {"-"};
        final String[] firstname = {"-"};


        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    BufferedReader bufferedReader = null;
                    String resultJSON = "";
                    url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        buffer.append(line);
                    }
                    resultJSON = buffer.toString();
                    JSONObject jsonObject = new JSONObject(urlConnection.toString());
                    Log.d("given",String.valueOf(jsonObject.getString("given_name")));
                    Log.d("family",String.valueOf(jsonObject.getString("family_name")));
                    lastname[0] = jsonObject.getString("given_name");
                    firstname[0] = jsonObject.getString("family_name");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

        thread.start();

        return  lastname[0] + " " + firstname[0];

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
    public void onItemClick(View v, int position) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        if (mRoomItems.get(position).Status==Values.ROOM_IS_FREE) {
            mSettings.setLastClickedAuditroom(mRoomItems.get(position).Auditroom);
            Bundle bundle = new Bundle();
            bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
            Nfc_Fragment nfc_fragment = Nfc_Fragment.newInstance();
            nfc_fragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, nfc_fragment, getResources().getString(R.string.fragment_tag_nfc)).commit();
        } else {
            if (mRoomItems.get(position).Access==Values.ACCESS_BY_CLICK) {
/*
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
                */
                new CloseRooms(mContext).execute(new CloseRoomsParams()
                        .setTag(mRoomItems.get(position).Tag)
                        .setRoomInterface(roomInterface));
            }else{
                Values.showFullscreenToast(mContext,getResources().getString(R.string.text_toast_put_card),Values.TOAST_NEGATIVE);
            }
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
        if (mRoomItems.get(position).Access==Values.ACCESS_BY_CARD){
            Dialog_Fragment dialog_fragment = new Dialog_Fragment();
            Bundle bundle = new Bundle();
            bundle.putLong(Values.POSITION_IN_BASE_FOR_ROOM,mRoomItems.get(position).PositionInBase);
            bundle.putString("aud",mRoomItems.get(position).Auditroom);
            bundle.putString("tag",mRoomItems.get(position).Tag);
            bundle.putLong("positionInBase",mRoomItems.get(position).PositionInBase);
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
    public void onRoomClosed(int closedRooms) {
        getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.fragment_tag_main)).commit();
        Values.showFullscreenToast(mContext, getResources().getString(R.string.text_toast_thanks), Values.TOAST_POSITIVE);
    }

}
