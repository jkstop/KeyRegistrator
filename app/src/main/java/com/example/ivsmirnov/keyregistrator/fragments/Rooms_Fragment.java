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
import android.widget.PopupMenu;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.GridRoomsAdapter;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

import at.markushi.ui.CircleButton;

/**
 * Created by IVSmirnov on 05.08.2015.
 */
public class Rooms_Fragment extends Fragment implements UpdateJournal {

    private GridView mGridView;
    private CircleButton mCircleButton;
    private ArrayList<Integer> mItems;
    private GridRoomsAdapter mGridRoomsAdapter;
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

        mGridRoomsAdapter = new GridRoomsAdapter(mContext, mItems);
        mGridView.setAdapter(mGridRoomsAdapter);
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

    private void showColumnsCount(View v){
        PopupMenu popupMenu = new PopupMenu(mContext,v);
        popupMenu.inflate(R.menu.popup_columns);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.two:
                        editor.putInt(Values.COLUMNS_COUNT, 2);
                        editor.commit();
                        return true;
                    case R.id.three:
                        editor.putInt(Values.COLUMNS_COUNT, 3);
                        editor.commit();
                        return true;
                    case R.id.four:
                        editor.putInt(Values.COLUMNS_COUNT, 4);
                        editor.commit();
                        return true;
                    case R.id.five:
                        editor.putInt(Values.COLUMNS_COUNT, 5);
                        editor.commit();
                        return true;
                    default:
                        return false;
                }
            }
        });

        for (int i = 0;i<popupMenu.getMenu().size();i++){
            MenuItem menuItem = popupMenu.getMenu().getItem(i);
            if (menuItem.getTitle().equals(String.valueOf(sharedPreferences.getInt(Values.COLUMNS_COUNT, 1)))){
                menuItem.setChecked(true);
            }
        }

        popupMenu.show();
    }

    @Override
    public void onDone() {
        DataBases db = new DataBases(mContext);
        mItems = new ArrayList<>(db.readFromRoomsDB());
        db.closeDBconnection();

        int columns = sharedPreferences.getInt(Values.COLUMNS_COUNT, 1);
        mGridRoomsAdapter = new GridRoomsAdapter(mContext, mItems);
        mGridView.setAdapter(mGridRoomsAdapter);
        mGridView.setNumColumns(columns);
    }
}
