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
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_main_auditrooms_grid;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

public class Main_Fragment extends Fragment implements UpdateMainFrame{

    public static GridView gridView;

    private SharedPreferences.Editor preferencesEditor;
    private SharedPreferences preferences;
    private Context context;
    static int selected_aud;

    private ArrayList <SparseArray<String>> mItems;
    private ArrayList<String> rooms;
    private ArrayList<Boolean> isFreeAud;
    private ArrayList<String> handOrCard;
    private ArrayList <String> lastVisiters;
    private ArrayList <String> photoPath;
    private ArrayList <String> tags;
    private LinearLayout disclaimer;
    private FrameLayout frameForGrid;

    adapter_main_auditrooms_grid adapter;

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
        context = rootView.getContext();

        rooms = new ArrayList<>();
        isFreeAud = new ArrayList<>();
        lastVisiters = new ArrayList<>();
        photoPath = new ArrayList<>();
        handOrCard = new ArrayList<>();
        tags = new ArrayList<>();

        DataBaseRooms dbRooms = new DataBaseRooms(context);
        mItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        for (int i=0;i<mItems.size();i++){
            rooms.add(mItems.get(i).get(0));
            isFreeAud.add(Boolean.parseBoolean(mItems.get(i).get(1)));
            handOrCard.add(mItems.get(i).get(2));
            lastVisiters.add(mItems.get(i).get(3));
            photoPath.add(mItems.get(i).get(5));
            tags.add(mItems.get(i).get(4));
        }

        frameForGrid = (FrameLayout) rootView.findViewById(R.id.frame_for_grid_aud);

        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int columns = preferences.getInt(Values.COLUMNS_AUD_COUNT, 1);
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        gridView.setNumColumns(columns);
        adapter = new adapter_main_auditrooms_grid(context,rooms,isFreeAud,lastVisiters,photoPath,tags);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                selected_aud = position;
                if (isFreeAud.get(position)) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                    bundle.putString(Values.AUDITROOM, view.getTag().toString());

                    Nfc_Fragment nfc_fragment = Nfc_Fragment.newInstance();
                    nfc_fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, nfc_fragment, getResources().getString(R.string.fragment_tag_nfc)).commit();
                } else {
                    if (handOrCard.get(position).equalsIgnoreCase("hand")) {
                        int pos = preferences.getInt(Values.POSITION_IN_BASE_FOR_ROOM + view.getTag().toString(), -1);

                        if (pos != -1) {
                            DataBaseJournal dbJournal = new DataBaseJournal(context);
                            dbJournal.updateDB(pos);
                            dbJournal.closeDB();
                        }
                        DataBaseRooms dbRooms = new DataBaseRooms(context);
                        dbRooms.updateStatusRooms(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + view.getTag(), -1), "true");
                        dbRooms.closeDB();

                        getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.fragment_tag_main)).commit();
                    }else{
                        Values.showFullscreenToast(context,getResources().getString(R.string.text_toast_put_card));
                    }
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (handOrCard.get(position).equalsIgnoreCase("card")){
                    Dialog_Fragment dialog_fragment = new Dialog_Fragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("aud",view.getTag().toString());
                    bundle.putInt(Values.DIALOG_CLOSE_ROOM_TYPE,Values.DIALOG_CLOSE_ROOM_TYPE_ROOMS);
                    bundle.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLOSE_ROOM);
                    dialog_fragment.setArguments(bundle);
                    dialog_fragment.show(getFragmentManager(),"enter_pin");
                }
                return true;
            }
        });

        TextView textEmptyAud = (TextView)rootView.findViewById(R.id.text_empty_aud_list);
        if (rooms.isEmpty()){
            textEmptyAud.setVisibility(View.VISIBLE);
        }else{
            textEmptyAud.setVisibility(View.INVISIBLE);
        }
        float disclaimer_size = preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15);
        disclaimer = (LinearLayout)rootView.findViewById(R.id.disclaimer);
        disclaimer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, disclaimer_size));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        DataBaseRooms dbRooms = new DataBaseRooms(context);
        mItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        rooms = new ArrayList<>();
        isFreeAud = new ArrayList<>();
        lastVisiters = new ArrayList<>();
        photoPath = new ArrayList<>();

        for (int i=0;i<mItems.size();i++){
            rooms.add(mItems.get(i).get(0));
            isFreeAud.add(Boolean.parseBoolean(mItems.get(i).get(1)));
            lastVisiters.add(mItems.get(i).get(3));
            photoPath.add(mItems.get(i).get(5));
            tags.add(mItems.get(i).get(4));
        }

        int columns = preferences.getInt(Values.COLUMNS_AUD_COUNT, 1);
        float grid_weight = preferences.getFloat(Values.GRID_SIZE, (float) 0.45);
        adapter = new adapter_main_auditrooms_grid(context, rooms, isFreeAud, lastVisiters, photoPath,tags);

        gridView.setAdapter(adapter);
        gridView.setNumColumns(columns);
        frameForGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, grid_weight));
        adapter.notifyDataSetChanged();

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
                //Save_to_server save_to_server = new Save_to_server(context);
                //save_to_server.execute();
                                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

    @Override
    public void onFinish() {
        float disclaimer_size = preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15);
        float grid_weight = preferences.getFloat(Values.GRID_SIZE, (float) 0.45);

        disclaimer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, disclaimer_size));
        frameForGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, grid_weight));
    }
}
