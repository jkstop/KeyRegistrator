package com.example.ivsmirnov.keyregistrator.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.databases.DataBasesRegist;
import com.example.ivsmirnov.keyregistrator.adapters.ListRoomsAdapter;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;


public class Rooms extends AppCompatActivity {

    private ListView listView;
    private TextView button;
    private ArrayList<Integer> items;
    private ListRoomsAdapter listRoomsAdapter;

    private int aud;
    private int pos;

    private Context context;

    private AlertDialog.Builder builder;

    private static final int DIALOG_NEW = 1;
    private static final int DIALOG_DELETE = 2;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        context = Rooms.this;

        Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
        toolbar.setTitle("Аудитории");
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        listView = (ListView)findViewById(R.id.list_rooms);
        button = (TextView)findViewById(R.id.buttonAddRoom);

        DataBases db = new DataBases(context);
        items = new ArrayList<>(db.readFromRoomsDB());
        db.closeDBconnection();

        listRoomsAdapter = new ListRoomsAdapter(context,items);
        listView.setAdapter(listRoomsAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle b = new Bundle();
                b.putInt("aud",items.get(position));
                b.putInt("pos",position);
                showDialog(DIALOG_DELETE, b);

                return true;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_NEW);
            }
        });
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        switch (id){
            case DIALOG_DELETE:
                aud = args.getInt("aud");
                pos = args.getInt("pos");
                dialog.setTitle("Удалить " + String.valueOf(aud) + " ?");
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id){
            case DIALOG_NEW:
                final EditText editText = new EditText(context);
                editText.setGravity(Gravity.CENTER);
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                builder = new AlertDialog.Builder(this);
                builder.setTitle("Новая запись")
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int item = Integer.parseInt(editText.getText().toString());
                                DataBases db = new DataBases(context);
                                db.writeInRoomsDB(item);
                                db.closeDBconnection();

                                items.add(item);
                                listRoomsAdapter.notifyDataSetChanged();
                                editText.setText("");
                                dialog.cancel();

                            }
                        });

                return builder.create();
            case DIALOG_DELETE:
                builder = new AlertDialog.Builder(context);
                builder.setTitle(" ")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataBases db = new DataBases(context);
                                db.cursorRoom.moveToPosition(pos);
                                db.deleteFromRoomsDB(sharedPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud,
                                        db.cursorRoom.getInt(db.cursorRoom.getColumnIndex(DataBasesRegist._ID))));
                                db.closeDBconnection();

                                items.remove(pos);
                                listRoomsAdapter.notifyDataSetChanged();

                                editor.remove(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + aud);
                                editor.remove(Values.POSITION_IN_BASE_FOR_ROOM + aud);
                                editor.remove(Values.POSITION_IN_LIST_FOR_ROOM + aud);
                                editor.commit();

                                dialog.cancel();
                            }
                        });
                return builder.create();
        }

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPause() {
        super.onPause();

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
