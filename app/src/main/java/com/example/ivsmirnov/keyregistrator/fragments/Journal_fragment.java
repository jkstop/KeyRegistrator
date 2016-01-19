package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_journal_list;
import com.example.ivsmirnov.keyregistrator.async_tasks.Load_from_server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_server;
import com.example.ivsmirnov.keyregistrator.custom_views.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Journal_fragment extends Fragment implements UpdateInterface,ActionBar.OnNavigationListener {

    private Context mContext;
    private RecyclerView mJournalRecycler;
    private ActionBar mActionBar;
    private ArrayList<String> mDates;
    private adapter_journal_list mAdapterjournallist;
    private ArrayList <JournalItem> mJournalItems;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public static Journal_fragment newInstance() {
        return new Journal_fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showDateSpinner();
    }

    public static ArrayList<String> calculateDates(Context context){
        ArrayList <String> items = new ArrayList<>();
        DataBaseJournal dbJournal = new DataBaseJournal(context);
        ArrayList<Long>datesLong = dbJournal.readJournalDatesFromDB();
        dbJournal.closeDB();
        DateFormat dateFormat = DateFormat.getDateInstance();
        for (int i = 0; i< datesLong.size(); i++){
            String dateString = dateFormat.format(new Date(datesLong.get(i)));
            if (!items.contains(dateString)){
                items.add(dateString);
            }
        }
        return  items;
    }

    private void showDateSpinner(){
        mActionBar = ((Launcher)getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(mContext,android.R.layout.simple_spinner_dropdown_item,mDates);
            mActionBar.setListNavigationCallbacks(spinnerAdapter,this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showDateSpinner();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_journal_fragment,container,false);
        mContext = rootView.getContext();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        mJournalRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_journal);
        mDates = calculateDates(mContext);
        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_journal, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_journal_save_to_file:
                Save_to_file saveToFile = new Save_to_file(mContext,Values.WRITE_JOURNAL);
                saveToFile.execute();
                return true;
            case R.id.menu_journal_download_to_server:
                Save_to_server save_to_server = new Save_to_server(mContext);
                save_to_server.execute();
                return true;
            case R.id.menu_journal_download_from_server:
                Load_from_server loadFromServer = new Load_from_server(mContext,this);
                loadFromServer.execute();
                return true;
            case R.id.menu_journal_download:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i,Values.LOAD_JOURNAL);
                return true;
            case R.id.menu_journal_delete:
                Dialog_Fragment dialog = new Dialog_Fragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Values.DIALOG_TYPE, Values.DIALOG_CLEAR_JOURNAL);
                dialog.setTargetFragment(this, 0);
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(),"clearJournal");
                return true;
            case R.id.menu_journal_select_location_for_copy:
                Intent iLC = new Intent(Intent.ACTION_GET_CONTENT);
                iLC.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                iLC.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                iLC.putExtra(FilePickerActivity.EXTRA_START_PATH,
                        mSharedPreferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL, Environment.getExternalStorageDirectory().getPath()));
                startActivityForResult(iLC,Values.SELECT_LOCATION_JOURNAL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (requestCode==Values.LOAD_JOURNAL){
                if (resultCode== Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    Loader_intent loader_intent = new Loader_intent(mContext,path,this,Values.LOAD_JOURNAL);
                    loader_intent.execute();
                }
            }else if (requestCode == Values.SELECT_LOCATION_JOURNAL){
                if (resultCode==Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    mSharedPreferencesEditor.putString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL,path);
                    mSharedPreferencesEditor.apply();
                }
            }
            //updateInformation();
        }
    }

    @Override
    public void updateInformation() {
        mDates = calculateDates(mContext);
        Date date = null;
        if (mDates.size()!=0){
            try {
                date = new SimpleDateFormat("dd MMM yyyy").parse(mDates.get(0));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            date = new Date(System.currentTimeMillis());
        }

        try {
            getJournalForDate(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        showDateSpinner();
    }
/*
    @Override
    public void onFinishEditing() {
        mDates = calculateDates(mContext);
        try {
            getJournalForDate(new SimpleDateFormat("dd MMM yyyy").parse(mDates.get(0)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        showDateSpinner();
    }
*/
    private void getJournalForDate(Date date){
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        mJournalItems = dbJournal.readJournalFromDB(date);
        dbJournal.closeDB();

        initializeJournal();
    }

    private void initializeJournal(){
        mAdapterjournallist = new adapter_journal_list(mContext, new RecycleItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

            }

            @Override
            public void onItemLongClick(View v, final int position, final long timeIn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
                                mJournalItems.remove(position);
                                mAdapterjournallist.notifyItemRemoved(position);

                                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                                dbJournal.deleteFromDB(timeIn);
                                dbJournal.closeDB();
                            }
                        })
                        .setCancelable(true);
                Dialog dialog = builder.create();
                dialog.show();
            }
        },mJournalItems);

        mJournalRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mJournalRecycler.setAdapter(mAdapterjournallist);
        mJournalRecycler.scrollToPosition(mJournalItems.size()-1);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Date date = null;
        try {
            date = new SimpleDateFormat("dd MMM yyyy").parse(mDates.get(itemPosition));
            getJournalForDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
