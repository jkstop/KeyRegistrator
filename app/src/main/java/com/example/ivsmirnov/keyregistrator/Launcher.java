package com.example.ivsmirnov.keyregistrator;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


public class Launcher extends ActionBarActivity {


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToogle;

    public static Context context;
    static String mPath;
    private SharedPreferences.Editor preferencesEditor;
    private SharedPreferences preferences;

    static int selected_aud;

    public static GridView gridView;
    private FrameLayout frameForGrid;

    private LinearLayout disclaimer;

    LayoutInflater inflater;
    View view;

    private ListView listView;
    public static adapter myListAdapter;
    ButtonsAdapter adapter;
    public static ArrayList<String> audList = new ArrayList<>();
    public static ArrayList<String> nameList = new ArrayList<>();
    public static ArrayList<Long> timeList = new ArrayList<>();
    public static ArrayList<Long> timePutList = new ArrayList<>();

    private DataBases db;

    private ArrayList<Integer> rooms;
    private ArrayList<Boolean> isFreeAud;
    private ArrayList <String> lastVisiters;

    public ButtonsAdapter btnsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        context = this;
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        mPath = Environment.getExternalStorageDirectory().getAbsolutePath();


        db = new DataBases(context);
        rooms = new ArrayList<>(db.readFromRoomsDB());
        isFreeAud = new ArrayList<>(db.readStatusRooms());
        lastVisiters = new ArrayList<>(db.readLastVisiterRoom());
        db.closeDBconnection();

        int columns = preferences.getInt(Values.COLUMNS_COUNT,1);

        gridView = (GridView)findViewById(R.id.gridView);
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
                        timePutList.set(preferences.getInt(Values.POSITION_IN_LIST_FOR_ROOM + view.getTag(), -1), System.currentTimeMillis());
                        myListAdapter.notifyDataSetChanged();
                    }

                    db.updateStatusRooms(preferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + view.getTag(), -1), 1);
                    db.closeDBconnection();
                    isFreeAud.set(position, true);
                    gridView.setAdapter(adapter);
                }
            }
        });
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });

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

        ArrayList<String> mDrawerTitles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.drawer_list)));
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView)findViewById(R.id.list_view_drawer);
        mDrawerPane = (RelativeLayout)findViewById(R.id.left_drawer);

        mDrawerListView.setAdapter(new NavDrawableAdapter(context,mDrawerTitles));
        mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToogle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToogle);

        if (savedInstanceState==null){
            setAlarm(closingTime());
        }


    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
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

    private void selectItem(int position){


        switch (position){
            case 0:
                startActivity(new Intent(context,Preferences.class));
                break;
            case 1:

                startActivity(new Intent(context, CloseDayDialog.class).putExtra("type", 1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case 2:
                startActivity(new Intent(context,Journal.class));
                break;
            default:
                break;
        }


        mDrawerListView.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    //закрытие всех позиций в 22.01
    public void setAlarm(Calendar calendar){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,CloseDayService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }else{
            alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        }

        preferencesEditor.putBoolean(Values.ALARM_SET, true);
        preferencesEditor.commit();

        Log.d("alarmSet","ok");
    }

    private Calendar closingTime(){
        Calendar now = Calendar.getInstance();
        Calendar when = (Calendar)now.clone();
        when.set(Calendar.HOUR_OF_DAY, 22);
        when.set(Calendar.MINUTE, 1);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);

        if (when.compareTo(now)<=0){
            when.add(Calendar.DATE,1);
        }
        return when;
    }

    @Override
    protected void onResume() {
        super.onResume();

        db = new DataBases(context);
        rooms = new ArrayList<>(db.readFromRoomsDB());
        isFreeAud = new ArrayList<>(db.readStatusRooms());
        lastVisiters = new ArrayList<>(db.readLastVisiterRoom());
        db.closeDBconnection();

        int columns = preferences.getInt(Values.COLUMNS_COUNT, 1);
        adapter = new ButtonsAdapter(context,rooms,isFreeAud,lastVisiters);
        TextView textEmptyAud = (TextView)findViewById(R.id.text_empty_aud_list);
        if (rooms.isEmpty()){
            textEmptyAud.setVisibility(View.VISIBLE);
        }else{
            textEmptyAud.setVisibility(View.INVISIBLE);
        }
        gridView.setAdapter(adapter);
        gridView.setNumColumns(columns);
        adapter.notifyDataSetChanged();

        if (audList.isEmpty()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    readJournalFromDB();
                }
            });
        }

        listView.setSelection(myListAdapter.getCount());

        float disclaimer_size = preferences.getFloat(Values.DISCLAIMER_SIZE, (float) 0.15);
        disclaimer = (LinearLayout)findViewById(R.id.disclaimer);
        disclaimer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, disclaimer_size));

        Boolean isAlarmSet = preferences.getBoolean(Values.ALARM_SET,false);
        Log.d("alarm",String.valueOf(isAlarmSet));
        if (!isAlarmSet){
            setAlarm(closingTime());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        audList.clear();
        nameList.clear();
        timeList.clear();
        timePutList.clear();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferencesEditor.remove(Values.ALARM_SET);
        preferencesEditor.commit();
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToogle.syncState();
    }

    public void ClickMenu(View view) {
        mDrawerLayout.openDrawer(mDrawerPane);
    }



}
