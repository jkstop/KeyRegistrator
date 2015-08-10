package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.base_sql_activity;
import com.example.ivsmirnov.keyregistrator.adapters.ButtonsAdapter;
import com.example.ivsmirnov.keyregistrator.adapters.ListAdapter;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.databases.DataBasesRegist;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateMainFrame;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 03.08.2015.
 */
public class Main_Fragment extends Fragment implements UpdateMainFrame{

    public static GridView gridView;

    private SharedPreferences.Editor preferencesEditor;
    private SharedPreferences preferences;
    private Context context;
    static int selected_aud;
    private DataBases db;

    private ArrayList<Integer> rooms;
    private ArrayList<Boolean> isFreeAud;
    private ArrayList <String> lastVisiters;
    private ArrayList <String> photoPath;
    private LinearLayout disclaimer;

    private ListView mListView;
    private ListAdapter mListAdapter;
    private ArrayList <SparseArray> mItems;

    ButtonsAdapter adapter;

    public static Main_Fragment newInstance (){
        Main_Fragment main_fragment = new Main_Fragment();
        return main_fragment;
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

        openBase();
        rooms = new ArrayList<>(db.readFromRoomsDB());
        isFreeAud = new ArrayList<>(db.readStatusRooms());
        lastVisiters = new ArrayList<>(db.readLastVisiterRoom());
        mItems = db.readJournalFromDB();
        closeBase();

        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int columns = preferences.getInt(Values.COLUMNS_COUNT, 1);
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        gridView.setNumColumns(columns);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                View viewGridItem = parent.getChildAt(position);
                TextView textButton = (TextView) viewGridItem.findViewById(R.id.textButton);
                selected_aud = position;

                if (isFreeAud.get(position)) {
                    startActivity(new Intent(context, base_sql_activity.class).putExtra(Values.AUDITROOM, view.getTag().toString()));
                } else {
                    int pos = preferences.getInt(Values.POSITION_IN_BASE_FOR_ROOM + view.getTag().toString(), -1);

                    textButton.setText(String.valueOf(view.getTag()));

                    viewGridItem.setBackgroundResource(R.drawable.button_background);
                    db = new DataBases(context);
                    if (pos == -1) {
                        Toast.makeText(context, "Был какой-то глюк...", Toast.LENGTH_SHORT).show();
                    } else {
                        db.updateDB(pos);
                        mItems.get(preferences.getInt(Values.POSITION_IN_LIST_FOR_ROOM+view.getTag(),-1)).put(3,String.valueOf(System.currentTimeMillis()));
                        mListAdapter.notifyDataSetChanged();
                    }

                    db.updateStatusRooms(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + view.getTag(), -1), 1);
                    db.closeDBconnection();
                    isFreeAud.set(position, true);
                    gridView.setAdapter(adapter);
                }
            }
        });

        mListView = (ListView)rootView.findViewById(R.id.list);
        mListAdapter = new ListAdapter(context,mItems);
        mListView.setAdapter(mListAdapter);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setSelection(mListAdapter.getCount());
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Удаление элемента")
                        .setMessage("Удалить выбранный элемент из списка?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mItems.remove(position);
                                mListAdapter.notifyDataSetChanged();

                                openBase();
                                db.deleteFromDB(position);
                                closeBase();
                            }
                        })
                        .setCancelable(true);
                Dialog dialog = builder.create();
                dialog.show();
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

        db = new DataBases(context);
        rooms = new ArrayList<>(db.readFromRoomsDB());
        isFreeAud = new ArrayList<>(db.readStatusRooms());
        lastVisiters = new ArrayList<>(db.readLastVisiterRoom());
        photoPath = new ArrayList<>(db.readPhotoPath());
        mItems = db.readJournalFromDB();
        db.closeDBconnection();

        mListAdapter = new ListAdapter(context,mItems);
        mListView.setAdapter(mListAdapter);
        mListView.setSelection(mListAdapter.getCount());

        int columns = preferences.getInt(Values.COLUMNS_COUNT, 1);
        adapter = new ButtonsAdapter(context,rooms,isFreeAud,lastVisiters,photoPath);

        gridView.setAdapter(adapter);
        gridView.setNumColumns(columns);
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
            case R.id.menu_main_disclaimer_size:
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Values.DIALOG_TYPE, Values.DIALOG_SEEKBAR);
                dialog.setArguments(bundle);
                dialog.setTargetFragment(this,0);
                dialog.show(getFragmentManager(), "seek");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onFinish() {
        float disclaimer_size = preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15);
        disclaimer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, disclaimer_size));
    }
}
