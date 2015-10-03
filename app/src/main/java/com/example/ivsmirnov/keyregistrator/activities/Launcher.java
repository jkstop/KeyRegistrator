package com.example.ivsmirnov.keyregistrator.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.fragments.Email_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Journal_fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Shedule_Fragment;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.CloseDayService;

import java.util.Calendar;

public class Launcher extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout_Drawer_root;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLinearLayout_Home, mLinearLayout_Settings, mLinearLayout_Statistics, mLinearLayout_Journal, mLinearLayout_Rooms, mLinearLayout_Email, mLinearLayout_Shedule;

    private Context mContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferencesEditor;

    private static long back_pressed;
    private static long symbol_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mContext = this;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_for_fragment, Main_Fragment.newInstance()).commit();

        Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
        toolbar.setTitle(getResources().getString(R.string.toolbar_title_main));

        setSupportActionBar(toolbar);

        mFrameLayout_Drawer_root = (FrameLayout)findViewById(R.id.main_activity_navigation_drawer_rootLayout);
        mLinearLayout_Home = (LinearLayout)findViewById(R.id.navigation_drawer_layout_home);
        mLinearLayout_Settings = (LinearLayout)findViewById(R.id.navigation_drawer_layout_settings);
        mLinearLayout_Statistics = (LinearLayout)findViewById(R.id.navigation_drawer_layout_statistics);
        mLinearLayout_Journal = (LinearLayout)findViewById(R.id.navigation_drawer_layout_journal);
        mLinearLayout_Rooms = (LinearLayout)findViewById(R.id.navigation_drawer_layout_rooms);
        mLinearLayout_Email = (LinearLayout) findViewById(R.id.navigation_drawer_layout_email);
        mLinearLayout_Shedule = (LinearLayout) findViewById(R.id.navigation_drawer_layout_shedule);

        mLinearLayout_Home.setOnClickListener(this);
        mLinearLayout_Settings.setOnClickListener(this);
        mLinearLayout_Statistics.setOnClickListener(this);
        mLinearLayout_Journal.setOnClickListener(this);
        mLinearLayout_Rooms.setOnClickListener(this);
        mLinearLayout_Email.setOnClickListener(this);
        mLinearLayout_Shedule.setOnClickListener(this);

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
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance())
                    .commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_persons))){
            if (getSupportActionBar()!=null){
                getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_persons));
            }
            Bundle bundle = new Bundle();
            bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_EDITOR);
            Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
            persons_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment,getResources().getString(R.string.tag_persons_fragment)).commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_statistics))){
            startActivity(new Intent(mContext, CloseDay.class).putExtra("type", 1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
        } else if (v.getTag().equals(getResources().getString(R.string.nav_drawer_item_email))) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_email));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Email_Fragment.newInstance()).commit();
        } else if (v.getTag().equals(getResources().getString(R.string.nav_drawer_item_shedule))) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(res.getString(R.string.nav_drawer_item_shedule));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Shedule_Fragment.newInstance()).commit();
        }else if (v.getTag().equals(res.getString(R.string.nav_drawer_item_about))){
            String version = "";
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
                version = packageInfo.versionName;

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(mContext,version,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext,"CLick",Toast.LENGTH_SHORT).show();
        }
        mDrawerLayout.closeDrawer(mFrameLayout_Drawer_root);
    }

    //закрытие всех позиций в 22.01
    public void setAlarm(Calendar calendar){
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext,CloseDayService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }else{
            alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        }

        mPreferencesEditor.putBoolean(Values.ALARM_SET, true);
        mPreferencesEditor.commit();
        Log.d("alarmSet", "ok");
    }

    private Calendar closingTime(){
        Calendar now = Calendar.getInstance();
        Calendar when = (Calendar)now.clone();
        when.set(Calendar.HOUR_OF_DAY, 22);
        when.set(Calendar.MINUTE, 1);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);

        if (when.compareTo(now)<=0){
            when.add(Calendar.DATE, 1);
        }
        return when;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Boolean isAlarmSet = mPreferences.getBoolean(Values.ALARM_SET,false);
        Log.d("alarm", String.valueOf(isAlarmSet));
        if (!isAlarmSet){
            setAlarm(closingTime());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreferencesEditor.remove(Values.ALARM_SET);
        mPreferencesEditor.commit();
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance())
                    .commit();
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (symbol_pressed + 2000 > System.currentTimeMillis()){
            super.onKeyDown(keyCode,event);
        }else{
            char key = (char)event.getUnicodeChar();
            try{
                if (getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_persons_fragment)).isVisible()){
                    Persons_Fragment.move(String.valueOf(key));
                }
            }catch (Exception e){
                Log.d("persons"," NOT visible");
            }
            symbol_pressed = System.currentTimeMillis();
        }

        return super.onKeyDown(keyCode, event);
    }
}
