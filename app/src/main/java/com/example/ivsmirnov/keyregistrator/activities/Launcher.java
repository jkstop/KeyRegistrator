package com.example.ivsmirnov.keyregistrator.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.adapters.ButtonsAdapter;
import com.example.ivsmirnov.keyregistrator.fragments.Journal_fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.services.CloseDayService;
import com.example.ivsmirnov.keyregistrator.R;

import java.util.Calendar;


public class Launcher extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout_Drawer_root;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLinearLayout_Home, mLinearLayout_Settings, mLinearLayout_Statistics, mLinearLayout_Journal,mLinearLayout_Rooms,
            mLinearLayout_About;



    public static Context context;
    static String mPath;

    public ButtonsAdapter btnsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        context = this;

        mPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_for_fragment, Main_Fragment.newInstance()).commit();



       // TextView head = (TextView)findViewById(R.id.head);
        //head.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
        toolbar.setTitle("Выберите нужную аудиторию нажатием на экран");

        setSupportActionBar(toolbar);

        mFrameLayout_Drawer_root = (FrameLayout)findViewById(R.id.main_activity_navigation_drawer_rootLayout);
        mLinearLayout_Home = (LinearLayout)findViewById(R.id.navigation_drawer_layout_home);
        mLinearLayout_Settings = (LinearLayout)findViewById(R.id.navigation_drawer_layout_settings);
        mLinearLayout_Statistics = (LinearLayout)findViewById(R.id.navigation_drawer_layout_statistics);
        mLinearLayout_Journal = (LinearLayout)findViewById(R.id.navigation_drawer_layout_journal);
        mLinearLayout_Rooms = (LinearLayout)findViewById(R.id.navigation_drawer_layout_rooms);
        mLinearLayout_About = (LinearLayout)findViewById(R.id.navigation_drawer_layout_about);

        mLinearLayout_Home.setOnClickListener(this);
        mLinearLayout_Settings.setOnClickListener(this);
        mLinearLayout_Statistics.setOnClickListener(this);
        mLinearLayout_Journal.setOnClickListener(this);
        mLinearLayout_Rooms.setOnClickListener(this);
        mLinearLayout_About.setOnClickListener(this);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_opened, R.string.navigation_drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

       if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mActionBarDrawerToggle.syncState();


    }

    @Override
    public void onClick(View v) {
        Resources res = getResources();

        if (v.getTag().equals(res.getString(R.string.nav_drawer_item_home))){
            if (getSupportActionBar()!=null){
                getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_main));
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_for_fragment,Main_Fragment.newInstance())
                    .commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_persons))){
            if (getSupportActionBar()!=null){
                getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_persons));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Persons_Fragment.newInstance()).commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_statistics))){
            startActivity(new Intent(context, CloseDayDialog.class).putExtra("type", 1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_journal))){
            if (getSupportActionBar()!=null){
                getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_journal));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Journal_fragment.newInstance()).commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_rooms))){
            if (getSupportActionBar()!=null){
                getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_auditrooms));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Rooms_Fragment.newInstance()).commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_about))){
            Toast.makeText(context,"about",Toast.LENGTH_SHORT).show();
        }else{

            Toast.makeText(context,"CLick",Toast.LENGTH_SHORT).show();
        }

        mDrawerLayout.closeDrawer(mFrameLayout_Drawer_root);
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

        //preferencesEditor.putBoolean(Values.ALARM_SET, true);
        //preferencesEditor.commit();

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


/*
        Boolean isAlarmSet = preferences.getBoolean(Values.ALARM_SET,false);
        Log.d("alarm",String.valueOf(isAlarmSet));
        if (!isAlarmSet){
            setAlarm(closingTime());
        }
*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //audList.clear();
        //nameList.clear();
        //timeList.clear();
        //timePutList.clear();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //preferencesEditor.remove(Values.ALARM_SET);
        //preferencesEditor.commit();
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        //mDrawerToogle.syncState();
    }

    public void ClickMenu(View view) {
        //mDrawerLayout.openDrawer(mDrawerPane);
    }



}
