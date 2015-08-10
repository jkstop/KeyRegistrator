package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ListView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.FileManager;
import com.example.ivsmirnov.keyregistrator.adapters.ListAdapter;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IVSmirnov on 04.08.2015.
 */
public class Journal_fragment extends Fragment implements UpdateJournal,Serializable {

    private Context mContext;
    private ListView mListView;

    private DataBases db;
    private ListAdapter mListAdapter;
    private ArrayList <SparseArray> mItems;

    private UpdateJournal listener;

    public static Journal_fragment newInstance(){
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
        readJournalFromDB();
        mListView = (ListView)rootView.findViewById(R.id.list);
        mListAdapter = new ListAdapter(mContext,mItems);
        mListView.setAdapter(mListAdapter);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setSelection(mListAdapter.getCount());
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
        return rootView;

    }

    private void readJournalFromDB(){
        openBase();
        mItems = db.readJournalFromDB();
        closeBase();
    }

    private void openBase(){
        db = new DataBases(mContext);
    }
    private void closeBase(){
        db.closeDBconnection();
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
                DataBases db = new DataBases(mContext);
                db.writeFile(Values.WRITE_JOURNAL);
                db.closeDBconnection();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String mPath = Environment.getExternalStorageDirectory().getPath();
                String path = preferences.getString(Values.PATH_FOR_COPY_ON_PC_FOR_JOURNAL, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                String srFileJournal = mPath + "/Journal.txt";
                String dtFileJournal = path + "/Journal.txt";
                DataBases.copyfile(mContext, srFileJournal, dtFileJournal);
                return true;
            case R.id.menu_journal_download:
                //startActivity(new Intent(mContext,FileManager.class).putExtra("what",10));
                startActivityForResult(new Intent(mContext,FileManager.class).putExtra("what",10),333);
                /*Dialog_Fragment fileManager = new Dialog_Fragment();
                Bundle b = new Bundle();
                b.putInt(Values.DIALOG_TYPE,456);
                fileManager.setTargetFragment(this,0);
                fileManager.setArguments(b);
                fileManager.show(getFragmentManager(),"showFileManager");*/
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
                startActivity(new Intent(mContext,FileManager.class).putExtra("buttonChoise", true).putExtra("pathFor","journal"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            onDone();
        }
    }

    @Override
    public void onDone() {
        Log.d("onDONE","!!!!!!!!!!!!");
        readJournalFromDB();
        mListAdapter = new ListAdapter(mContext,mItems);
        mListView.setAdapter(mListAdapter);
    }
}
