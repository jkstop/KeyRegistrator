package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.adapters.base_sql_activity_adapter;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import at.markushi.ui.CircleButton;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity extends ActionBarActivity implements UpdateTeachers {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private static long today,lastDate;

    private Context context;
    public static GridView gridView;
    private DataBases db;
    private CircleButton mAddButton;

    ArrayList<SparseArray> allItems;
    public static base_sql_activity_adapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_sql_activity);

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        gridView = (GridView)findViewById(R.id.grid_for_base_sql);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - parent.getFirstVisiblePosition();
                View rootView = parent.getChildAt(pos);
                TextView textSurname = (TextView) rootView.findViewById(R.id.text_familia);
                TextView textName = (TextView) rootView.findViewById(R.id.text_imya);
                TextView textLastName = (TextView) rootView.findViewById(R.id.otchestvo);
                TextView textKaf = (TextView)rootView.findViewById(R.id.kafedra);

                String aud = getIntent().getStringExtra(Values.AUDITROOM);
                String name = textSurname.getText().toString() + " "
                        + textName.getText().toString().charAt(0) + "." +
                        textLastName.getText().toString().charAt(0) + ".";
                final Long time = System.currentTimeMillis();

                openBase();
                String path = db.findPhotoPath(new String[]{textSurname.getText().toString(), textName.getText().toString(),
                        textLastName.getText().toString(), textKaf.getText().toString()});
                closeBase();
                writeIt(aud, name, time, path);

                finish();
            }
        });


    }

    private void writeIt (String aud,String name,Long time,String path){
        openBase();
        if (today==lastDate){
            db.writeInDBJournal(aud,name,time,(long)0,false);
            editor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, db.cursorJournal.getCount());
        }else{
            db.writeInDBJournalHeaderDate();
            editor.putInt(Values.CURSOR_POSITION, db.cursorJournal.getCount());
            editor.commit();
            db.writeInDBJournal(aud, name, time, (long) 0,false);
            editor.putInt(Values.POSITION_IN_LIST_FOR_ROOM + aud, db.cursorJournal.getCount()+1);
        }
        db.updateStatusRooms(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), 0);
        db.updateLastVisitersRoom(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud, -1), name);
        db.updatePhotoPath(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud,-1),path);
        closeBase();

        editor.putLong(Values.DATE, today);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Calendar calendar = Calendar.getInstance();
        today = calendar.get(Calendar.DATE);
        lastDate = sharedPreferences.getLong(Values.DATE, 0);

        openBase();
        allItems = db.readTeachersFromDB();
        closeBase();

        sortByABC();

        adapter = new base_sql_activity_adapter(context,allItems);
        gridView.setAdapter(adapter);
    }


    private void sortByABC(){
        Collections.sort(allItems, new Comparator<SparseArray>() {
            @Override
            public int compare(SparseArray lhs, SparseArray rhs) {
                String first = String.valueOf(lhs.get(0));
                String second = String.valueOf(rhs.get(0));
                return first.compareToIgnoreCase(second);
            }
        });
    }

    public void showAddUserDialog(View view) {
       /* Dialog_Fragment dialogType = new Dialog_Fragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt(Values.DIALOG_TYPE,Values.INPUT_DIALOG);
        dialogType.setArguments(bundle1);
        dialogType.show(getSupportFragmentManager(), "type");*/
        startActivity(new Intent(this,Add_user.class));
    }

    private void openBase(){
        db = new DataBases(context);
    }
    private void closeBase(){
        db.closeDBconnection();
    }


    @Override
    public void onFinishEditing() {
        openBase();
        allItems = db.readTeachersFromDB();
        closeBase();

        sortByABC();
        adapter = new base_sql_activity_adapter(context,allItems);
        gridView.setAdapter(adapter);
    }
}
