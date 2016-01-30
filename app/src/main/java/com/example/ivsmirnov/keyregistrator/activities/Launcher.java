package com.example.ivsmirnov.keyregistrator.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_navigation_drawer_list;
import com.example.ivsmirnov.keyregistrator.async_tasks.Get_Account_Information;
import com.example.ivsmirnov.keyregistrator.async_tasks.Open_Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Power_Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Protocol_Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Tag_Reader;
import com.example.ivsmirnov.keyregistrator.custom_views.AccountItem;
import com.example.ivsmirnov.keyregistrator.custom_views.NavigationItem;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
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
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.CloseDayService;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class Launcher extends AppCompatActivity implements GetUserByTag, Get_Account_Information_Interface{

    private DrawerLayout mDrawerLayout;
    private LinearLayout mLayout_Drawer_root;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private RelativeLayout mDrawerAccountView;
    private ListView mNavigationDrawerList;
    private adapter_navigation_drawer_list mNavigationDrawerListAdapter;
    private TextView mAccountName, mAccountEmail;
    private ImageView mChangeAccount, mPersonAccountImage;

    private Context mContext;
    private Resources mResources;
    private Settings mSettings;

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



        if (savedInstanceState==null){
            showFragment(Main_Fragment.newInstance(),R.string.fragment_tag_main);
            //toolbar.setTitle(getResources().getString(R.string.toolbar_title_main));
        }

        mContext = this;
        mResources = getResources();
        mSettings = new Settings(mContext);

        SQL_Connector.check_sql_connection(mContext,null,null,null);

        initNavigationDrawer();

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
                        try {
                            if (stateStrings[finalCurrState].equals("Present")) {
                                Persons_Fragment persons_fragment = (Persons_Fragment) getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.fragment_tag_persons));
                                Nfc_Fragment nfc_fragment = (Nfc_Fragment) getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.fragment_tag_nfc));
                                Main_Fragment main_fragment = (Main_Fragment) getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.fragment_tag_main));
                                if (persons_fragment != null && persons_fragment.isVisible()) {
                                    Log.d("persons_fragment", "visible");
                                    aud = null;
                                    powerReader();
                                    setProtocol();
                                    getTag();
                                } else if (nfc_fragment != null && nfc_fragment.isVisible()) {
                                    aud = getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.fragment_tag_nfc)).getArguments().getString(Values.AUDITROOM);
                                    powerReader();
                                    setProtocol();
                                    getTag();

                                } else if (main_fragment != null && main_fragment.isVisible()) {
                                    isOpened = true;
                                    powerReader();
                                    setProtocol();
                                    getTag();
                                } else {
                                    Toast.makeText(mContext, "Not one of yet", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }

    // Navigation Drawer initialization; Toolbar initialization
    private void initNavigationDrawer(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mLayout_Drawer_root = (LinearLayout)findViewById(R.id.main_activity_navigation_drawer_rootLayout);
        mNavigationDrawerList = (ListView)findViewById(R.id.left_navigation_drawer_list);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerAccountView = (RelativeLayout)findViewById(R.id.navigation_drawer_account_view);
        mAccountName = (TextView)findViewById(R.id.navigation_drawer_account_information_display_name);
        mAccountEmail = (TextView)findViewById(R.id.navigation_drawer_account_information_email);
        mChangeAccount = (ImageView)findViewById(R.id.navigation_drawer_account_information_change_account);
        mPersonAccountImage = (ImageView)findViewById(R.id.navigation_drawer_account_information_image_person);

        mLayout_Drawer_root.getLayoutParams().width = (int) calculateDrawerWidth();
        mDrawerAccountView.getLayoutParams().height = (int) calculateDrawerHeight(mLayout_Drawer_root.getLayoutParams().width);

        ArrayList<NavigationItem> mNavigationItems = new ArrayList<>();
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_home)).setIcon(R.drawable.ic_home_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_persons)).setIcon(R.drawable.ic_person_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_journal)).setIcon(R.drawable.ic_format_list_bulleted_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_rooms)).setIcon(R.drawable.ic_room_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_shedule)).setIcon(R.drawable.ic_grid_on_black_24dp));
        mNavigationItems.add(new NavigationItem().setSeparator(true));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_mail)).setIcon(R.drawable.ic_attachment_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_sql)).setIcon(R.drawable.ic_backup_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_stat)).setIcon(R.drawable.ic_info_outline_black_24dp));

        mNavigationDrawerListAdapter = new adapter_navigation_drawer_list(mContext,mNavigationItems);
        mNavigationDrawerList.setAdapter(mNavigationDrawerListAdapter);
        mNavigationDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem  = mNavigationDrawerListAdapter.getItemText(position);
                if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_home))){
                    showFragment(Main_Fragment.newInstance(),R.string.fragment_tag_main);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_persons))){
                    setToolbarTitle(R.string.toolbar_title_persons);
                    Bundle bundle = new Bundle();
                    bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_EDITOR);
                    Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                    persons_fragment.setArguments(bundle);
                    showFragment(persons_fragment,R.string.fragment_tag_persons);
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_journal))){
                    setToolbarTitle(R.string.toolbar_title_journal);
                    showFragment(Journal_fragment.newInstance(),R.string.fragment_tag_journal);
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_rooms))){
                    setToolbarTitle(R.string.toolbar_title_auditrooms);
                    showFragment(Rooms_Fragment.newInstance(),R.string.fragment_tag_rooms);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_shedule))){
                    setToolbarTitle(R.string.toolbar_title_shedule);
                    showFragment(Shedule_Fragment.newInstance(),R.string.fragment_tag_shedule);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_mail))){
                    setToolbarTitle(R.string.toolbar_title_email);
                    showFragment(Email_Fragment.newInstance(),R.string.fragment_tag_email);
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_sql))){
                    Dialog_Fragment dialog_sql = new Dialog_Fragment();
                    Bundle bundle_sql = new Bundle();
                    bundle_sql.putInt(Values.DIALOG_TYPE,Values.DIALOG_SQL_CONNECT);
                    dialog_sql.setArguments(bundle_sql);
                    dialog_sql.show(getSupportFragmentManager(),"sql");
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_stat))) {
                    startActivity(new Intent(mContext, CloseDay.class).putExtra("type", 1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

                mDrawerLayout.closeDrawer(mLayout_Drawer_root);
            }
        });

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
        mActionBarDrawerToggle.syncState();

        mChangeAccount.setOnClickListener(changeAccountClick);

        getUserActiveAccount();
    }

    View.OnClickListener changeAccountClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                        false, null, null, null, null);
                startActivityForResult(intent, 123);
            }catch (ActivityNotFoundException e){
                Toast.makeText(mContext,"Сервисы Google Play не установлены!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123){
            if (resultCode == Activity.RESULT_OK){
                try {
                    Get_Account_Information get_account_information = new Get_Account_Information(mContext,data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME),this);
                    get_account_information.execute();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void getUserActiveAccount(){

        DataBaseAccount dbAccount = new DataBaseAccount(mContext);
        AccountItem mAccount = dbAccount.getAccount(mSettings.getActiveAccountID());
        dbAccount.closeDB();

        if (mAccount!=null){
            String text = mAccount.Lastname+ " "+mAccount.Firstname;
            mAccountName.setText(text);
            mAccountEmail.setText(mAccount.Email);
            new loadImageFromWeb(mAccount.Photo).execute();
        }
    }



    private class loadImageFromWeb extends AsyncTask<Void,Drawable,Drawable>{

        private String mUrl;

        private loadImageFromWeb(String url){
            this.mUrl = url;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            try{
                InputStream inputStream = (InputStream)new URL(mUrl).getContent();
                return Drawable.createFromStream(inputStream,"photo");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable!=null){
                mPersonAccountImage.setImageDrawable(drawable);
            }
        }
    }


    private float calculateDrawerWidth(){
        int viewMin = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.actionBarSize,typedValue,true);
        int actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data,getResources().getDisplayMetrics());
        int widht = viewMin - actionBarSize;
        return Math.min(widht,getResources().getDimension(R.dimen.navigation_drawer_widht));
    }

    private float calculateDrawerHeight(int widht){
        int acpestRatioHeight = Math.round(widht/ 16*9);
        int minHeiht = 16;
        return Math.max(acpestRatioHeight,minHeiht);
    }

    private String getStringFromResources(int resId){
        return mResources.getString(resId);
    }

    private void setToolbarTitle (int resId){
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(mResources.getString(resId));
        }
    }

    private void showFragment(Fragment fragment, int fragmentTagId){
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment,fragment,getResources().getString(fragmentTagId)).commit();
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
            ArrayList <RoomItem> rms = dbRooms.readRoomsDB();

            int closedRooms = 0;

            for (int i=0;i<rms.size();i++){
                if (rms.get(i).Status==0){
                    if (items.get(7).equalsIgnoreCase(rms.get(i).Tag)){
                        long pos = rms.get(i).PositionInBase;
                        if (pos != -1) {
                            dbJournal.updateDB(rms.get(i).PositionInBase);
                            closedRooms++;
                        }
                        //dbRooms.updateStatusRooms(mPreferences.getInt(Values.POSITION_IN_ROOMS_BASE_FOR_ROOM + rms.get(i).get(0), -1), "true");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(), getResources().getString(R.string.fragment_tag_main)).commit();
                    }
                }
            }
            if (closedRooms==0){
                Values.showFullscreenToast(mContext,getResources().getString(R.string.text_toast_choise_room_in_first));
            }

            dbRooms.closeDB();
            dbJournal.closeDB();
            isOpened = false;
        }else{
            if (aud!=null){
                if (items.get(2).equals("Аноним")){
                    LayoutInflater inflater = getLayoutInflater();
                    View toastLayout = inflater.inflate(R.layout.layout_toast_incorrect_card,
                            (ViewGroup)findViewById(R.id.toast_nfc_incorrect_root));
                    Toast toast = new Toast(mContext);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(toastLayout);
                    toast.show();
                }else{
                    String name  = items.get(2) + " "
                            + items.get(3).charAt(0) + "." +
                            items.get(4).charAt(0) + ".";
                    //Persons_Fragment.writeIt(mContext,aud,name,System.currentTimeMillis(),items.get(6),items.get(7),"card");
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_for_fragment,Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main)).commit();
                }
            }else{
                String [] values = new String[]{items.get(2),items.get(3),items.get(4),items.get(0),items.get(5),items.get(6)};
                Bundle b = new Bundle();
                b.putInt(Values.DIALOG_TYPE, Values.DIALOG_EDIT);
                b.putStringArray("valuesForEdit", values);
                Dialog_Fragment dialog = new Dialog_Fragment();
                dialog.setArguments(b);
                dialog.setTargetFragment(getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.fragment_tag_persons)), 0);
                dialog.show(getSupportFragmentManager(),"edit");
            }
        }
    }

    @Override
    public void onUserRecoverableAuthException(UserRecoverableAuthException e) {
    }

    @Override
    public void onChangeAccount() {
        initNavigationDrawer();
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

        mSettings.setAutoCloseStatus(true);
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

        if (!mSettings.getAutoCloseStatus()){
            setAlarm(closingTime());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSettings.cleanAutoCloseStatus();
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
                    .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main))
                    .commit();
            back_pressed = System.currentTimeMillis();
        }
    }

}
