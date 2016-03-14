package com.example.ivsmirnov.keyregistrator.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.async_tasks.LoadImageFromWeb;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.TakeKey;
import com.example.ivsmirnov.keyregistrator.databases.DB;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.fragments.Search_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.DBinterface;
import com.example.ivsmirnov.keyregistrator.interfaces.KeyInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.ReaderResponse;
import com.example.ivsmirnov.keyregistrator.interfaces.RoomInterface;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.CloseRoomsParams;
import com.example.ivsmirnov.keyregistrator.items.NavigationItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Email_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Journal_fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Nfc_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Shedule_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.items.TakeKeyParams;

import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.Alarm;
import com.example.ivsmirnov.keyregistrator.services.NFC_Reader;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;


public class Launcher extends AppCompatActivity implements Get_Account_Information_Interface,
        KeyInterface, RoomInterface, GoogleApiClient.OnConnectionFailedListener, DBinterface{

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
    private FragmentActivity mFragmentActivity;

    private KeyInterface mKeyInterface;
    public static RoomInterface mRoomInterface;

    public static GoogleApiClient mGoogleApiClient;

    private static long back_pressed;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };
    private UsbManager mUsbManager;
    private Reader mReader;

    private boolean isMainNavigationVisible;
    private ArrayList<AccountItem> mAccountItems;

    String aud = null;
    Boolean isOpened = false;

    private Alarm mAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        if (savedInstanceState==null){
            showFragment(Main_Fragment.newInstance(),R.string.fragment_tag_main);
        }

        mContext = this;
        mResources = getResources();

        new DB();
        new Settings();

        mAlarm = new Alarm(getApplicationContext());

        mKeyInterface = this;
        mRoomInterface = this;
        mFragmentActivity = this;


        mAlarm.setAlarm(closingTime());

        new SQL_Connection(mContext, Settings.getServerConnectionParams(), null).execute();

        initNavigationDrawer(getMainNavigationItems());

        initGoogleAPI();
        new initReader().execute();
    }

    private void initGoogleAPI(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage(this,this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Log.d("google_api","inited");
    }

    // Navigation Drawer initialization; Toolbar initialization
    private void initNavigationDrawer(ArrayList<NavigationItem> navigationItems){
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

        mNavigationDrawerListAdapter = new adapter_navigation_drawer_list(mContext,navigationItems);
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

        mChangeAccount.setOnClickListener(logOutClick);
        mAccountName.setOnClickListener(logOnClick);

        getUserActiveAccount();
    }

    private ArrayList<NavigationItem> getMainNavigationItems(){
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
        isMainNavigationVisible = true;
        return mNavigationItems;
    }


    View.OnClickListener logOutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Dialog_Fragment dialog_fragment = new Dialog_Fragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Values.DIALOG_TYPE, Values.DIALOG_LOG_OUT);
            dialog_fragment.setArguments(bundle);
            dialog_fragment.show(getSupportFragmentManager(),"dialog_logout");
        }
    };

    View.OnClickListener logOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, Values.REQUEST_CODE_LOG_ON);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == Values.REQUEST_CODE_LOG_ON){
                Log.d("request","logon");
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                if (result.isSuccess()) {
                    final GoogleSignInAccount acct = result.getSignInAccount();
                    AccountItem accountItem = new AccountItem().setLastname(acct.getDisplayName())
                            .setEmail(acct.getEmail())
                            .setPhoto(acct.getPhotoUrl().toString())
                            .setAccountID(acct.getId());

                    DataBaseAccount.writeAccount(accountItem);

                    DataBaseFavorite.writeInDBTeachers(mContext, new PersonItem()
                            .setLastname(acct.getDisplayName())
                            .setDivision(acct.getEmail())
                            .setRadioLabel(acct.getId()));


                    Settings.setActiveAccountID(acct.getId());
                    //Settings.setAuthToken(acct.getIdToken());
                    initNavigationDrawer(getMainNavigationItems());
                } else {
                    Toast.makeText(mContext,"Не удалось подключиться", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getUserActiveAccount(){

        AccountItem mAccount = DataBaseAccount.getAccount(Settings.getActiveAccountID());

        if (mAccount!=null){
            String text = mAccount.getLastname();
            mAccountName.setText(text);
            mAccountEmail.setText(mAccount.getEmail());
            new LoadImageFromWeb(mAccount.getPhoto(), this).execute();
        } else {
            mAccountName.setText(getStringFromResources(R.string.navigation_drawer_account_info_text_user));
            mAccountEmail.setText(Values.EMPTY);
            mPersonAccountImage.setImageDrawable(null);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    @Override
    public void onUserRecoverableAuthException(UserRecoverableAuthException e) {

    }

    @Override
    public void onChangeAccount() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
            }
        });
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
            }
        });
        Settings.setActiveAccountID("localAccount");
        getUserActiveAccount();
    }

    @Override
    public void onAccountImageLoaded(Bitmap bitmap) {
        mPersonAccountImage.setImageBitmap(bitmap);
        new updateUserAccount().execute(bitmap);
    }

    //обновление аккаунта в базе
    private class updateUserAccount extends AsyncTask<Bitmap, Void, Void>{

        @Override
        protected Void doInBackground(Bitmap... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            params[0].compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String photo = Base64.encodeToString(byteArray,Base64.NO_WRAP);

            if (DataBaseFavorite.getPersonItem(mContext, Settings.getActiveAccountID(), DataBaseFavorite.LOCAL_USER, -1)!=null){
                DataBaseFavorite.writeInDBTeachers(mContext, DataBaseFavorite.getPersonItem(mContext, Settings.getActiveAccountID(),DataBaseFavorite.LOCAL_USER, -1)
                        .setPhotoOriginal(photo)
                        .setPhotoPreview(DataBaseFavorite.getPhotoPreview(photo)));
            } else {
                AccountItem account = DataBaseAccount.getAccount(Settings.getActiveAccountID());
                DataBaseFavorite.writeInDBTeachers(mContext,new PersonItem()
                        .setRadioLabel(account.getAccountID())
                        .setLastname(account.getLastname())
                        .setDivision(account.getEmail())
                        .setPhotoOriginal(photo)
                        .setPhotoPreview(DataBaseFavorite.getPhotoPreview(photo)));
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("Launcher","RESUME");
        Log.d("CHECK_ALARM", String.valueOf(mAlarm.isAlarmSet()));

        if (!mAlarm.isAlarmSet()){
            mAlarm.setAlarm(closingTime());
            Log.d("SET_ALARM", String.valueOf(mAlarm.isAlarmSet()));
        }

        //new DB();
        //new Settings();
    }

    public long closingTime(){
        Calendar now = Calendar.getInstance();
        Calendar when = (Calendar)now.clone();
        when.set(Calendar.HOUR_OF_DAY, 22);
        when.set(Calendar.MINUTE, 1);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);

        if (when.compareTo(now)<=0){
            when.add(Calendar.DATE, 1);
        }
        return when.getTimeInMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("Launcher","DESTROY");

        DB.closeDB();

        if (mReader!=null){
            mReader.close();
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAlarm!=null)  mAlarm.cancelAlarm();

        Log.d("CHECK_ALARM", String.valueOf(mAlarm.isAlarmSet()));
        Log.d("Launcher","STOP");
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Search_Fragment search_fragment = (Search_Fragment) getFragmentByTag(R.string.fragment_tag_search);
            if (search_fragment!=null && search_fragment.isVisible()){
                Persons_Fragment persons_fragment = Persons_Fragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putInt(Values.PERSONS_FRAGMENT_TYPE, Values.PERSONS_FRAGMENT_SELECTOR);
                bundle.putString(Values.AUDITROOM, Settings.getLastClickedAuditroom());
                persons_fragment.setArguments(bundle);
                showFragment(persons_fragment, R.string.fragment_tag_main);
            }else{
                Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main))
                        .commit();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onDBinited(boolean isInited) {

    }

    private class initReader extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {

            mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
            mReader = new Reader(mUsbManager);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(Values.ACTION_USB_PERMISSION), 0);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Values.ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

            registerReceiver(mReceiver, filter);

            for (UsbDevice device : mUsbManager.getDeviceList().values()) {
                mUsbManager.requestPermission(device,mPermissionIntent);
            }

            mReader.setOnStateChangeListener(new com.acs.smartcard.Reader.OnStateChangeListener() {
                @Override
                public void onStateChange(int i, int prevState, int currState) {

                    if (currState < com.acs.smartcard.Reader.CARD_UNKNOWN
                            || currState > com.acs.smartcard.Reader.CARD_SPECIFIC) {
                        currState = com.acs.smartcard.Reader.CARD_UNKNOWN;
                    }

                    final int finalCurrState = currState;
                    try {
                        if (stateStrings[finalCurrState].equals("Present")) {

                            new NFC_Reader().getTag(mReader, new ReaderResponse() {
                                @Override
                                public void onGetResult(String tag) {
                                    Persons_Fragment persons_fragment = (Persons_Fragment) getFragmentByTag(R.string.fragment_tag_persons);
                                    Nfc_Fragment nfc_fragment = (Nfc_Fragment)getFragmentByTag(R.string.fragment_tag_nfc);
                                    Main_Fragment main_fragment = (Main_Fragment) getFragmentByTag(R.string.fragment_tag_main);

                                    if (persons_fragment != null && persons_fragment.isVisible()) {

                                        new getPerson(getPerson.PERSONS).execute(tag);

                                    } else if (nfc_fragment != null && nfc_fragment.isVisible()) {

                                        new getPerson(getPerson.NFS).execute(tag);

                                    } else if (main_fragment != null && main_fragment.isVisible()) {

                                        if (DataBaseRooms.getRoomItemForCurrentUser(tag)!=null){
                                            new CloseRooms(mContext).execute(new CloseRoomsParams()
                                                    .setTag(tag)
                                                    .setRoomInterface(mRoomInterface));
                                        }else{
                                            Values.showFullscreenToast(mContext, getStringFromResources(R.string.text_toast_choise_room_in_first), Values.TOAST_NEGATIVE);
                                        }

                                    } else {
                                        Log.d("not","one");
                                    }
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }
    }

    @Override
    public void onTakeKey() {
        showFragment(Main_Fragment.newInstance(), R.string.fragment_tag_main);
    }

    @Override
    public void onRoomClosed() {
        showFragment(Main_Fragment.newInstance(), R.string.fragment_tag_main);
    }

    private Fragment getFragmentByTag(int resID){
        return getSupportFragmentManager().findFragmentByTag(getResources().getString(resID));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())){
                synchronized (this){
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
                        if (device!=null){
                            new NFC_Reader.openReader(mReader).execute(device);
                        }
                    }
                }
            }
        }
    };

    private class getPerson extends AsyncTask <String, Void, PersonItem> {

        public static final int NFS = 0;
        public static final int PERSONS = 1;

        private int mType;

        public getPerson(int type){
            this.mType = type;
        };


        @Override
        protected PersonItem doInBackground(String... params) {

            PersonItem personItem = DataBaseFavorite.getPersonItem(mContext, params[0], DataBaseFavorite.LOCAL_USER, DataBaseFavorite.PREVIEW_PHOTO);

            if (personItem == null){
                personItem = DataBaseFavorite.getPersonItem(mContext, params[0], DataBaseFavorite.SERVER_USER, DataBaseFavorite.FULLSIZE_PHOTO);
                personItem.setPhotoPreview(DataBaseFavorite.getPhotoPreview(personItem.getPhotoOriginal()));

                DataBaseFavorite.writeInDBTeachers(mContext, personItem);
                personItem = DataBaseFavorite.getPersonItem(mContext, params[0], DataBaseFavorite.LOCAL_USER, DataBaseFavorite.PREVIEW_PHOTO);
            }

            return personItem;
        }

        @Override
        protected void onPostExecute(PersonItem personItem) {

            if (personItem!=null&&!personItem.isEmpty()){
                if (mType == NFS){
                    new TakeKey(mContext).execute(new TakeKeyParams()
                            .setPersonItem(personItem)
                            .setAuditroom(Settings.getLastClickedAuditroom())
                            .setAccessType(DataBaseJournal.ACCESS_BY_CARD)
                            .setPublicInterface(mKeyInterface));
                } else if (mType == PERSONS){
                    Persons_Fragment persons_fragment = (Persons_Fragment) getFragmentByTag(R.string.fragment_tag_persons);

                    Bundle b = new Bundle();
                    b.putInt(Values.DIALOG_TYPE, Values.DIALOG_EDIT);
                    b.putString(Values.DIALOG_PERSON_INFORMATION_KEY_TAG, personItem.getRadioLabel());

                    Dialog_Fragment dialog = new Dialog_Fragment();
                    dialog.setArguments(b);
                    dialog.setTargetFragment(persons_fragment, 0);
                    dialog.show(persons_fragment.getChildFragmentManager(), "edit");
                }

            }else{
                Log.d("wrong","card!!!");
            }
        }
    }

}
