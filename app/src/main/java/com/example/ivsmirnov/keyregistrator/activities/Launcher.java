package com.example.ivsmirnov.keyregistrator.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.Open_Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Power_Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Protocol_Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Tag_Reader;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Email_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Journal_fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Nfc_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Shedule_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.GetUserByTag;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.CloseDayService;

import java.util.ArrayList;
import java.util.Calendar;

public class Launcher extends AppCompatActivity implements View.OnClickListener,GetUserByTag{

    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout_Drawer_root;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLinearLayout_Home, mLinearLayout_Settings, mLinearLayout_Statistics, mLinearLayout_Journal, mLinearLayout_Rooms, mLinearLayout_Email, mLinearLayout_Shedule;

    private Context mContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferencesEditor;

    private static long back_pressed;
    private static long symbol_pressed;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };
    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    String aud = null;
    Boolean isOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mContext = this;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        //init reader
        mManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mReader = new Reader(mManager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
        for (UsbDevice device : mManager.getDeviceList().values()) {
            mManager.requestPermission(device,mPermissionIntent);
        }
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
            @Override
            public void onStateChange(int i, int prevState, int currState) {

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                final int finalCurrState = currState;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stateStrings[finalCurrState].equals("Present")){
                            Persons_Fragment persons_fragment = (Persons_Fragment)getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_persons_fragment));
                            Nfc_Fragment nfc_fragment = (Nfc_Fragment)getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_nfc_fragment));
                            Main_Fragment main_fragment = (Main_Fragment)getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_main_fragment));
                            if (persons_fragment!=null&&persons_fragment.isVisible()){
                                Log.d("persons_fragment","visible");
                                aud = null;
                                powerReader();
                                setProtocol();
                                getTag();
                            }else if (nfc_fragment!=null&&nfc_fragment.isVisible()){
                                aud = getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_nfc_fragment)).getArguments().getString(Values.AUDITROOM);
                                powerReader();
                                setProtocol();
                                getTag();
                            }else if (main_fragment!=null&&main_fragment.isVisible()){
                                isOpened = true;
                                powerReader();
                                setProtocol();
                                getTag();
                            }else{
                                Toast.makeText(mContext,"Not one of yet",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.tag_main_fragment)).commit();

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

    private void powerReader (){
        int slotNum = 0;
        int actionNum = 2;

        PowerParams params = new PowerParams();
        params.slotNum = slotNum;
        params.action = actionNum;

        Power_Reader powerTask = new Power_Reader(mReader);
        powerTask.execute(params);
    }

    @Override
    public void onGetSparse(SparseArray<String> items) {

        if (isOpened){
            DataBaseRooms dbRooms = new DataBaseRooms(mContext);
            DataBaseJournal dbJournal = new DataBaseJournal(mContext);
            ArrayList <SparseArray<String>> rms = dbRooms.readRoomsDB();

            for (int i=0;i<rms.size();i++){
                if (rms.get(i).get(1).equalsIgnoreCase("false")){
                    if (items.get(7).equalsIgnoreCase(rms.get(i).get(4))){
                        int pos = mPreferences.getInt(Values.POSITION_IN_BASE_FOR_ROOM + rms.get(i).get(0), -1);
                        if (pos != -1) {
                            dbJournal.updateDB(pos);
                        }
                        dbRooms.updateStatusRooms(mPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + rms.get(i).get(0), -1), "true");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.tag_main_fragment)).commit();
                    }
                }
            }
            dbRooms.closeDB();
            dbJournal.closeDB();
            isOpened = false;
        }else{
            if (aud!=null){
                if (items.get(2).equals("Аноним")){
                    Bundle bundle = new Bundle();
                    bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                    bundle.putInt(Values.PERSONS_FRAGMENT_HEAD,Values.PERSONS_FRAGMENT_HEAD_NOT_FOUND_USER);
                    bundle.putString(Values.AUDITROOM, aud);
                    Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                    persons_fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, persons_fragment, getResources().getString(R.string.tag_persons_fragment)).commit();
                }else{
                    String name  = items.get(2) + " "
                            + items.get(3).charAt(0) + "." +
                            items.get(4).charAt(0) + ".";
                    Persons_Fragment.writeIt(mContext,aud,name,System.currentTimeMillis(),items.get(6),items.get(7),"card");
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment,Main_Fragment.newInstance(),getResources().getString(R.string.tag_main_fragment)).commit();
                }
            }else{
                String [] values = new String[]{items.get(2),items.get(3),items.get(4),items.get(0),items.get(5),items.get(6)};
                Bundle b = new Bundle();
                b.putInt(Values.DIALOG_TYPE, Values.DIALOG_EDIT);
                b.putStringArray("valuesForEdit", values);
                Dialog_Fragment dialog = new Dialog_Fragment();
                dialog.setArguments(b);
                dialog.setTargetFragment(getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_persons_fragment)), 0);
                dialog.show(getSupportFragmentManager(),"edit");
            }
        }

    }

    public static class PowerParams {
        public int slotNum;
        public int action;
    }
    public static class PowerResult {
        public byte[] atr;
        public Exception e;
    }

    private void setProtocol(){
        int slotNum = 0;
        int prefferedProtocol = Reader.PROTOCOL_UNDEFINED;
        String prefferedProtocolString = "";
        prefferedProtocol |= Reader.PROTOCOL_T0;
        prefferedProtocolString = "T0";
        prefferedProtocol |= Reader.PROTOCOL_T1;
        prefferedProtocolString +="/T1";
        if (prefferedProtocolString==""){
            prefferedProtocolString="None";
        }
        SetProtocolParams params = new SetProtocolParams();
        params.slotNum = slotNum;
        params.preferredProtocols = prefferedProtocol;
        Protocol_Reader protocol_task = new Protocol_Reader(mReader);
        protocol_task.execute(params);
    }
    public static class SetProtocolParams {
        public int slotNum;
        public int preferredProtocols;
    }
    public static class SetProtocolResult {
        public int activeProtocol;
        public Exception e;
    }

    private void getTag(){
        int slotNum = 0;
        TransmitParams transmitParams = new TransmitParams();
        transmitParams.slotNum = slotNum;
        transmitParams.controlCode = -1;
        transmitParams.command = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04};
        Tag_Reader transmit_task = new Tag_Reader(mContext,mReader,this);
        transmit_task.execute(transmitParams);
    }
    public static class TransmitParams {
        public int slotNum;
        public int controlCode;
        public byte [] command;
    }
    public static class TransmitResult {
        public byte[] response;
        public int responseLength;
        public Exception e;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
                        if (device!=null){
                            Log.d("opening","reader...");
                            Open_Reader open_reader = new Open_Reader(mReader);
                            open_reader.execute(device);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        Resources res = getResources();

        if (v.getTag().equals(res.getString(R.string.nav_drawer_item_home))){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.tag_main_fragment))
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
        mReader.close();
        unregisterReceiver(mReceiver);
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
