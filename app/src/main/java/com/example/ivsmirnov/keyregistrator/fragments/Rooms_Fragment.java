package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

import at.markushi.ui.CircleButton;

public class Rooms_Fragment extends Fragment implements UpdateJournal {

    private GridView mGridView;
    private CircleButton mCircleButton;
    private ArrayList<Integer> mItems;
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

        DataBases db = new DataBases(mContext);
        mItems = new ArrayList<>(db.readFromRoomsDB());
        db.closeDBconnection();

        int columns = sharedPreferences.getInt(Values.COLUMNS_COUNT, 1);

        mAdaptereditauditroomsgrid = new adapter_edit_auditrooms_grid(mContext, mItems);
        mGridView.setAdapter(mAdaptereditauditroomsgrid);
        mGridView.setNumColumns(columns);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle b = new Bundle();
                b.putInt("aud", mItems.get(position));
                b.putInt("pos", position);
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
                //showColumnsCount(mGridView);
                Dialog_Fragment dialog_fragment = new Dialog_Fragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Values.DIALOG_TYPE,Values.SELECT_COLUMNS_DIALOG);
                dialog_fragment.setArguments(bundle);
                dialog_fragment.setTargetFragment(Rooms_Fragment.this,0);
                dialog_fragment.show(getFragmentManager(),"columns");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDone() {
        DataBases db = new DataBases(mContext);
        mItems = new ArrayList<>(db.readFromRoomsDB());
        db.closeDBconnection();

        int columns = sharedPreferences.getInt(Values.COLUMNS_COUNT, 1);
        mAdaptereditauditroomsgrid = new adapter_edit_auditrooms_grid(mContext, mItems);
        mGridView.setAdapter(mAdaptereditauditroomsgrid);
        mGridView.setNumColumns(columns);
    }
}
