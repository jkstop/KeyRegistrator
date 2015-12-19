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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_journal_list;
import com.example.ivsmirnov.keyregistrator.async_tasks.Load_from_server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_intent;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_file;
import com.example.ivsmirnov.keyregistrator.async_tasks.Save_to_server;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;


public class Journal_fragment extends Fragment implements UpdateJournal,UpdateTeachers {

    private Context mContext;
    private ListView mListView;

    private adapter_journal_list mAdapterjournallist;
    private ArrayList <SparseArray> mItems;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_journal_fragment,container,false);
        mContext = rootView.getContext();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        mItems = dbJournal.readJournalFromDB();
        dbJournal.closeDB();

        mListView = (ListView)rootView.findViewById(R.id.list);
        mAdapterjournallist = new adapter_journal_list(mContext, mItems);
        mListView.setAdapter(mAdapterjournallist);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setSelection(mAdapterjournallist.getCount());
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
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
                                mItems.remove(position);
                                mAdapterjournallist.notifyDataSetChanged();

                                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                                dbJournal.deleteFromDB(position);
                                dbJournal.closeDB();
                            }
                        })
                        .setCancelable(true);
                Dialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

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
            onFinishEditing();
        }
    }

    @Override
    public void onDone() {
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        dbJournal = new DataBaseJournal(mContext);
        mItems = dbJournal.readJournalFromDB();
        mAdapterjournallist = new adapter_journal_list(mContext, mItems);
        mListView.setAdapter(mAdapterjournallist);
        dbJournal.closeDB();
    }

    @Override
    public void onFinishEditing() {
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        mItems = dbJournal.readJournalFromDB();
        mAdapterjournallist = new adapter_journal_list(mContext, mItems);
        mListView.setAdapter(mAdapterjournallist);
        dbJournal.closeDB();
    }
}
