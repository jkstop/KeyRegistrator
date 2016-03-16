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
import com.example.ivsmirnov.keyregistrator.adapters.AdapterNavigationDrawerList;
import com.example.ivsmirnov.keyregistrator.async_tasks.CloseRooms;
import com.example.ivsmirnov.keyregistrator.async_tasks.LoadImageFromWeb;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.DB;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.fragments.Search_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.ReaderInterface;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.NavigationItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.fragments.Dialogs;
import com.example.ivsmirnov.keyregistrator.fragments.Email_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Journal_fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Nfc_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Shedule_Fragment;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;

import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.Alarm;
import com.example.ivsmirnov.keyregistrator.services.NFC_Reader;
import com.example.ivsmirnov.keyregistrator.services.Toasts;
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


public class Launcher extends AppCompatActivity implements GetAccountInterface, BaseWriterInterface, CloseRoomInterface,GoogleApiClient.OnConnectionFailedListener{

    //constants for reader
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

    public static final int REQUEST_CODE_LOG_ON = 205;

    private Context mContext;
    private Resources mResources;

    //adapters
    private AdapterNavigationDrawerList mAdapterNavigationDrawerList;

    //navigation drawer
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerRootLayout;

    //account
    private TextView mAccountName, mAccountEmail;
    private ImageView mAccountImage;

    //interfaces
    private GetAccountInterface mGetAccountInterface;
    private BaseWriterInterface mBaseWriterInterface;
    private CloseRoomInterface mCloseRoomInterface;

    private GoogleApiClient mGoogleApiClient;

    private static long back_pressed;

    private Reader mReader;

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

        //init DataBases
        new DB();

        //init SharedPreferences
        new Settings();

        //connect to server
        new SQL_Connection(mContext, Settings.getServerConnectionParams(), null).execute();

        //init interfaces
        mBaseWriterInterface = this;
        mGetAccountInterface = this;
        mCloseRoomInterface = this;

        //init and set auto close alarm
        mAlarm = new Alarm(App.getAppContext());
        mAlarm.setAlarm(mAlarm.closingTime());

        initNavigationDrawer();

        initGoogleAPI();

        new initReader().execute();
    }

    private void initGoogleAPI(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    // Navigation Drawer init; Toolbar init
    private void initNavigationDrawer(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mDrawerRootLayout = (LinearLayout)findViewById(R.id.main_activity_navigation_drawer_rootLayout);
        ListView mNavigationDrawerList = (ListView) findViewById(R.id.left_navigation_drawer_list);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        RelativeLayout mDrawerAccountView = (RelativeLayout) findViewById(R.id.navigation_drawer_account_view);
        mAccountName = (TextView)findViewById(R.id.navigation_drawer_account_information_display_name);
        mAccountEmail = (TextView)findViewById(R.id.navigation_drawer_account_information_email);
        ImageView mChangeAccount = (ImageView) findViewById(R.id.navigation_drawer_account_information_change_account);
        mAccountImage = (ImageView)findViewById(R.id.navigation_drawer_account_information_image_person);

        mDrawerRootLayout.getLayoutParams().width = (int) calculateDrawerWidth();
        mDrawerAccountView.getLayoutParams().height = (int) calculateDrawerHeight(mDrawerRootLayout.getLayoutParams().width);

        //set NavigationItems
        ArrayList<NavigationItem> mNavigationItems = new ArrayList<>();
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_home)).setIcon(R.drawable.ic_home_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_persons)).setIcon(R.drawable.ic_person_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_journal)).setIcon(R.drawable.ic_format_list_bulleted_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_rooms)).setIcon(R.drawable.ic_room_black_24dp));
       // mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_shedule)).setIcon(R.drawable.ic_grid_on_black_24dp));
        mNavigationItems.add(new NavigationItem().setSeparator(true));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_mail)).setIcon(R.drawable.ic_attachment_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_sql)).setIcon(R.drawable.ic_backup_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_stat)).setIcon(R.drawable.ic_info_outline_black_24dp));

        mAdapterNavigationDrawerList = new AdapterNavigationDrawerList(mContext, mNavigationItems);
        mNavigationDrawerList.setAdapter(mAdapterNavigationDrawerList);
        mNavigationDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem  = mAdapterNavigationDrawerList.getItemText(position);
                if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_home))){
                    showFragment(Main_Fragment.newInstance(),R.string.fragment_tag_main);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_persons))){
                    setToolbarTitle(R.string.toolbar_title_persons);
                    Bundle bundle = new Bundle();
                    bundle.putInt(Persons_Fragment.PERSONS_FRAGMENT_TYPE, Persons_Fragment.PERSONS_FRAGMENT_EDITOR);
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
                    Dialogs dialog_sql = new Dialogs();
                    Bundle bundle_sql = new Bundle();
                    bundle_sql.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_SQL_CONNECT);
                    dialog_sql.setArguments(bundle_sql);
                    dialog_sql.show(getSupportFragmentManager(),"sql");
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_stat))) {
                    startActivity(new Intent(mContext, CloseDay.class).putExtra("type", 1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

                mDrawerLayout.closeDrawer(mDrawerRootLayout);
            }
        });

        ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_opened, R.string.navigation_drawer_closed) {
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


    View.OnClickListener logOutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Dialogs dialogs = new Dialogs();
            Bundle bundle = new Bundle();
            bundle.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_LOG_OUT);
            dialogs.setArguments(bundle);
            dialogs.show(getSupportFragmentManager(),"dialog_logout");
        }
    };

    View.OnClickListener logOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, REQUEST_CODE_LOG_ON);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

            if (resultCode == Activity.RESULT_OK){
                if (requestCode == REQUEST_CODE_LOG_ON){

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
                        initNavigationDrawer();
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
            new LoadImageFromWeb(mAccount.getPhoto(), mGetAccountInterface).execute();
        } else {
            mAccountName.setText(getStringFromResources(R.string.navigation_drawer_account_info_text_user));
            mAccountEmail.setText(Values.EMPTY);
            mAccountImage.setImageDrawable(null);
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
        startActivityForResult(e.getIntent(),123);
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
        mAccountImage.setImageBitmap(bitmap);
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

            if (DataBaseFavorite.isUserInBase(Settings.getActiveAccountID())){
                DataBaseFavorite.writeInDBTeachers(mContext, DataBaseFavorite.getPersonItem(mContext, Settings.getActiveAccountID(),DataBaseFavorite.LOCAL_USER, DataBaseFavorite.NO_PHOTO)
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
        if (!mAlarm.isAlarmSet()) {
            mAlarm.setAlarm(mAlarm.closingTime());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //close databases
        DB.closeDB();

        //close reader
        try {
            if (mReader!=null){
                mReader.close();
                unregisterReceiver(mReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAlarm!=null)  mAlarm.cancelAlarm();
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
                bundle.putInt(Persons_Fragment.PERSONS_FRAGMENT_TYPE, Persons_Fragment.PERSONS_FRAGMENT_SELECTOR);
                bundle.putString(Settings.AUDITROOM, Settings.getLastClickedAuditroom());
                persons_fragment.setArguments(bundle);
                showFragment(persons_fragment, R.string.fragment_tag_main);
            }else{
                Toast.makeText(getBaseContext(), getStringFromResources(R.string.toast_press_back_again), Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_for_fragment, Main_Fragment.newInstance(),getResources().getString(R.string.fragment_tag_main))
                        .commit();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    private class initReader extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {

            UsbManager mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
            mReader = new Reader(mUsbManager);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_USB_PERMISSION);
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

                            new NFC_Reader().getTag(mReader, new ReaderInterface() {
                                @Override
                                public void onGetPersonTag(String tag) {
                                    Persons_Fragment persons_fragment = (Persons_Fragment) getFragmentByTag(R.string.fragment_tag_persons);
                                    Nfc_Fragment nfc_fragment = (Nfc_Fragment)getFragmentByTag(R.string.fragment_tag_nfc);
                                    Main_Fragment main_fragment = (Main_Fragment) getFragmentByTag(R.string.fragment_tag_main);

                                    if (persons_fragment != null && persons_fragment.isVisible()) {

                                        new GetUser(GetUser.PERSONS).execute(tag);

                                    } else if (nfc_fragment != null && nfc_fragment.isVisible()) {

                                        new GetUser(GetUser.NFC).execute(tag);

                                    } else if (main_fragment != null && main_fragment.isVisible()) {

                                        if (DataBaseRooms.getRoomItemForCurrentUser(tag)!=null){
                                            new CloseRooms(mContext, tag, mCloseRoomInterface).execute();
                                        }else{
                                            Toasts.showFullscreenToast(mContext, getStringFromResources(R.string.text_toast_choise_room_in_first), Toasts.TOAST_NEGATIVE);
                                        }
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
    public void onSuccessBaseWrite() {
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

    //поиск пользователя в базе; выдача ключей (type = NFC) или показ информации (type = PERSONS)
    private class GetUser extends AsyncTask <String, Void, String> {

        public static final int NFC = 0;
        public static final int PERSONS = 1;

        private boolean mValidUser = true;
        private int mType;

        public GetUser(int type){
          this.mType = type;
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                //если нет в базе, то добавить
                if (!DataBaseFavorite.isUserInBase(params[0])){
                    mValidUser = DataBaseFavorite.writeInDBTeachers(mContext, DataBaseFavorite.getPersonItem(mContext, params[0], DataBaseFavorite.SERVER_USER, DataBaseFavorite.ALL_PHOTO));
                }
                return params[0];
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String personTag) {
            if (personTag!=null && mValidUser){
                switch (mType){
                    case NFC:
                        new BaseWriter(mContext, mBaseWriterInterface).execute(new BaseWriterParams()
                                .setPersonTag(personTag)
                                .setAuditroom(Settings.getLastClickedAuditroom())
                                .setAccessType(DataBaseJournal.ACCESS_BY_CARD));
                        break;
                    case PERSONS:
                        Persons_Fragment persons_fragment = (Persons_Fragment) getFragmentByTag(R.string.fragment_tag_persons);

                        Bundle b = new Bundle();
                        b.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_EDIT);
                        b.putString(Dialogs.DIALOG_PERSON_INFORMATION_KEY_TAG, personTag);

                        Dialogs dialog = new Dialogs();
                        dialog.setArguments(b);
                        dialog.setTargetFragment(persons_fragment, 0);
                        dialog.show(persons_fragment.getChildFragmentManager(), "edit");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
