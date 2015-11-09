package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.Write_File;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

public class Main_Fragment extends Fragment implements UpdateMainFrame{

    public static GridView gridView;

    private SharedPreferences.Editor preferencesEditor;
    private SharedPreferences preferences;
    private Context context;
    static int selected_aud;
    private DataBases db;

    private ArrayList <SparseArray<String>> mItems;
    private ArrayList<String> rooms;
    private ArrayList<Boolean> isFreeAud;
    private ArrayList <String> lastVisiters;
    private ArrayList <String> photoPath;
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

        final DataBaseRooms dbRooms = new DataBaseRooms(context);
        mItems = dbRooms.readRoomsDB();

        for (int i=0;i<mItems.size();i++){
            rooms.add(mItems.get(i).get(0));
            isFreeAud.add(Boolean.parseBoolean(mItems.get(i).get(1)));
            lastVisiters.add(mItems.get(i).get(3));
            photoPath.add(mItems.get(i).get(5));
        }

        frameForGrid = (FrameLayout) rootView.findViewById(R.id.frame_for_grid_aud);

        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int columns = preferences.getInt(Values.COLUMNS_AUD_COUNT, 1);
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        gridView.setNumColumns(columns);
        adapter = new adapter_main_auditrooms_grid(context,rooms,isFreeAud,lastVisiters,photoPath);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                View viewGridItem = parent.getChildAt(position);
                TextView textButton = (TextView) viewGridItem.findViewById(R.id.textButton);
                selected_aud = position;
                if (isFreeAud.get(position)) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                    bundle.putString(Values.AUDITROOM, view.getTag().toString());
                    //Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                    //persons_fragment.setArguments(bundle);
                    //getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.tag_persons_fragment)).commit();
                    Nfc_Fragment nfc_fragment = Nfc_Fragment.newInstance();
                    nfc_fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment,nfc_fragment,getResources().getString(R.string.tag_nfc_fragment)).commit();
                } else {
                    int pos = preferences.getInt(Values.POSITION_IN_BASE_FOR_ROOM + view.getTag().toString(), -1);

                    //textButton.setText(String.valueOf(view.getTag()));

                    //viewGridItem.setBackgroundResource(R.drawable.button_background);
                    db = new DataBases(context);
                    if (pos != -1) {
                        db.updateDB(pos);
                    }

                    dbRooms.updateStatusRooms(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + view.getTag(), -1), "true");
                    getFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.tag_main_fragment)).commit();
                    //db.closeDBconnection();
                    //isFreeAud.set(position, true);
                    //gridView.setAdapter(adapter);
                }
                lastClickTime = SystemClock.elapsedRealtime();
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

        rooms = new ArrayList<>();
        isFreeAud = new ArrayList<>();
        lastVisiters = new ArrayList<>();
        photoPath = new ArrayList<>();

        DataBaseRooms dbRooms = new DataBaseRooms(context);
        mItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        for (int i=0;i<mItems.size();i++){
            rooms.add(mItems.get(i).get(0));
            isFreeAud.add(Boolean.parseBoolean(mItems.get(i).get(1)));
            lastVisiters.add(mItems.get(i).get(3));
            photoPath.add(mItems.get(i).get(5));
        }

        int columns = preferences.getInt(Values.COLUMNS_AUD_COUNT, 1);
        float grid_weight = preferences.getFloat(Values.GRID_SIZE, (float) 0.45);
        adapter = new adapter_main_auditrooms_grid(context, rooms, isFreeAud, lastVisiters, photoPath);

        gridView.setAdapter(adapter);
        gridView.setNumColumns(columns);
        frameForGrid.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, grid_weight));
        adapter.notifyDataSetChanged();

    }

    private void openBase(){
        db = new DataBases(context);
    }
    private void closeBase(){
        db.closeDBconnection();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher) getActivity()).getSupportActionBar() != null) {
            ((Launcher) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_main));
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
