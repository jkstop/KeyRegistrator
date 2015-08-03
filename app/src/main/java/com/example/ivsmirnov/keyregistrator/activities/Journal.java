package com.example.ivsmirnov.keyregistrator.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.databases.DataBasesRegist;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter;

import java.util.ArrayList;

/**
 * Created by ivsmirnov on 18.06.2015.
 */
public class Journal extends AppCompatActivity {
    private ListView listView;
    private static Context context;
    private DataBases db;
    public static adapter myListAdapter;
    public static ArrayList<String> audList = new ArrayList<>();
    public static ArrayList<String> nameList = new ArrayList<>();
    public static ArrayList<Long> timeList = new ArrayList<>();
    public static ArrayList<Long> timePutList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_journal);

        context = this;

        initialiseToolbar();

        listView = (ListView)findViewById(R.id.list);
        myListAdapter = new adapter(context,audList,nameList,timeList,timePutList);
        listView.setAdapter(myListAdapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setSelection(myListAdapter.getCount());
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
                                audList.remove(position);
                                nameList.remove(position);
                                timeList.remove(position);
                                timePutList.remove(position);
                                myListAdapter.notifyDataSetChanged();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audList.isEmpty()){
            readJournalFromDB();
        }
        listView.setSelection(myListAdapter.getCount());
    }

    @Override
    protected void onPause() {
        super.onPause();
        audList.clear();
        nameList.clear();
        timeList.clear();
        timePutList.clear();
    }

    private void openBase(){
        db = new DataBases(context);
    }
    private void closeBase(){
        db.closeDBconnection();
    }

    private void readJournalFromDB(){
        openBase();
        db.cursorJournal.moveToPosition(-1);
        while (db.cursorJournal.moveToNext()){
            String aud = db.cursorJournal.getString(db.cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_AUD));
            String name = db.cursorJournal.getString(db.cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_NAME));
            Long time = db.cursorJournal.getLong(db.cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME));
            Long timePut = db.cursorJournal.getLong(db.cursorJournal.getColumnIndex(DataBasesRegist.COLUMN_TIME_PUT));
            audList.add(aud);
            nameList.add(name);
            timeList.add(time);
            timePutList.add(timePut);
            myListAdapter.notifyDataSetChanged();
        }
        Log.d("read journal", "ok");
        closeBase();
    }

    private void initialiseToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
