package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_edit_auditrooms_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.FinishLoad;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

import at.markushi.ui.CircleButton;

public class Rooms_Fragment extends Fragment implements UpdateTeachers,FinishLoad {

    private GridView mGridView;
    private CircleButton mCircleButton;
    private ArrayList<SparseArray<String>> mItems;
    private ArrayList<String> mRooms;
    private adapter_edit_auditrooms_grid mAdaptereditauditroomsgrid;
    private Context mContext;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static Rooms_Fragment newInstance(){
        return new Rooms_Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_auditrooms_fragment,container,false);
        mContext = rootView.getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        mGridView = (GridView)rootView.findViewById(R.id.grid_rooms);
        mCircleButton = (CircleButton)rootView.findViewById(R.id.buttonAddRoom);

        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        mRooms = new ArrayList<>();
        for (int i=0;i<mItems.size();i++){
            mRooms.add(mItems.get(i).get(0));
        }

        int columns = sharedPreferences.getInt(Values.COLUMNS_AUD_COUNT, 1);

        mAdaptereditauditroomsgrid = new adapter_edit_auditrooms_grid(mContext, mRooms);
        mGridView.setAdapter(mAdaptereditauditroomsgrid);
        mGridView.setNumColumns(columns);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle b = new Bundle();
                b.putString("aud", mRooms.get(position));
                b.putInt(Values.DIALOG_TYPE,Values.DELETE_ROOM_DIALOG);
                dialog.setArguments(b);
                dialog.setTargetFragment(Rooms_Fragment.this,0);
                dialog.show(getFragmentManager(),"delete_room");
            }
        });

        mCircleButton.setOnClickListener(new View.OnClickListener() {
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
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_auditrooms, menu);
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
                Save_to_file saveToFile = new Save_to_file(mContext,Values.WRITE_ROOMS);
                saveToFile.execute();
                return true;
            case R.id.menu_auditrooms_load_from_file:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Values.LOAD_ROOMS);
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
    public void onFinishEditing() {
        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        mRooms = new ArrayList<>();
        for (int i=0;i<mItems.size();i++){
            mRooms.add(mItems.get(i).get(0));
        }

        int columns = sharedPreferences.getInt(Values.COLUMNS_AUD_COUNT, 1);
        mAdaptereditauditroomsgrid = new adapter_edit_auditrooms_grid(mContext, mRooms);
        mGridView.setAdapter(mAdaptereditauditroomsgrid);
        mGridView.setNumColumns(columns);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (requestCode == Values.LOAD_ROOMS){
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    Loader_intent loader_intent = new Loader_intent(mContext,path,this,Values.LOAD_ROOMS);
                    loader_intent.execute();
                }
            }
            onFinishEditing();

        }
    }

    @Override
    public void onFinish() {
        DataBaseRooms dbRooms = new DataBaseRooms(mContext);
        mItems = dbRooms.readRoomsDB();
        dbRooms.closeDB();

        mRooms = new ArrayList<>();
        for (int i=0;i<mItems.size();i++){
            mRooms.add(mItems.get(i).get(0));
        }

        int columns = sharedPreferences.getInt(Values.COLUMNS_AUD_COUNT, 1);
        mAdaptereditauditroomsgrid = new adapter_edit_auditrooms_grid(mContext, mRooms);
        mGridView.setAdapter(mAdaptereditauditroomsgrid);
        mGridView.setNumColumns(columns);
    }
}
