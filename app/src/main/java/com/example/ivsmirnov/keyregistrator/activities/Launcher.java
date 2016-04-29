package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.databases.DbShare;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.MainFr;
import com.example.ivsmirnov.keyregistrator.fragments.PersonsFr;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.NavigationItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.AccountDB;
import com.example.ivsmirnov.keyregistrator.fragments.Dialogs;
import com.example.ivsmirnov.keyregistrator.fragments.EmailFr;
import com.example.ivsmirnov.keyregistrator.fragments.JournalFr;
import com.example.ivsmirnov.keyregistrator.fragments.RoomsFr;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;

import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.services.Alarm;
import com.example.ivsmirnov.keyregistrator.services.NFC;
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
import com.squareup.picasso.Picasso;

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

    //NFC reader
    private NFC mNFCReader;

    //adapters
    public static AdapterNavigationDrawerList mAdapterNavigationDrawerList;

    //navigation drawer
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerRootLayout;
    public static ArrayList<NavigationItem> mNavigationItems;
    private ListView mNavigationDrawerList;

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
    public static Reader.OnStateChangeListener sReaderStateChangeListener;
    public static boolean sCardConnected = false;

    private Alarm mAlarm;

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("START");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("CREATE");
        setContentView(R.layout.launcher);

       // ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
       /// ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
       // System.out.println("ACTIVITY " + this.getClass().getCanonicalName());

        mContext = this;
        mResources = getResources();

        mNavigationItems = new ArrayList<>();

        //init interfaces
        mBaseWriterInterface = this;
        mGetAccountInterface = this;
        mCloseRoomInterface = this;

        //connect to server
        new SQL_Connection(Settings.getServerConnectionParams(), null).execute();

        //init DataBases
        new DbShare();

        //init SharedPreferences
        new Settings();

        //init and set auto close alarm
        setSheduler();

        initGoogleAPI();

        //new initReader().execute();


        initUI();

        if (savedInstanceState == null){
            showFragment(getSupportFragmentManager(), MainFr.newInstance(),R.string.navigation_drawer_item_home);

            mNFCReader = new NFC();
        }

    }

    private void initUI (){
        //элементы бокового меню
        if (!mNavigationItems.isEmpty()) mNavigationItems.clear();
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_home)).setIcon(R.drawable.ic_home_black_24dp).setSelected(true));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_persons)).setIcon(R.drawable.ic_person_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_journal)).setIcon(R.drawable.ic_format_list_bulleted_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_rooms)).setIcon(R.drawable.ic_room_black_24dp));
        mNavigationItems.add(new NavigationItem().setSeparator(true));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_settings)).setIcon(R.drawable.ic_settings_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_mail)).setIcon(R.drawable.ic_attachment_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_sql)).setIcon(R.drawable.ic_cloud_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_stat)).setIcon(R.drawable.ic_info_black_24dp));

        //тулбар
        Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDrawerRootLayout = (LinearLayout)findViewById(R.id.main_activity_navigation_drawer_rootLayout);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mAccountName = (TextView)findViewById(R.id.navigation_drawer_account_information_display_name);
        mAccountEmail = (TextView)findViewById(R.id.navigation_drawer_account_information_email);
        mAccountImage = (ImageView)findViewById(R.id.navigation_drawer_account_information_image_person);

        mNavigationDrawerList = (ListView) findViewById(R.id.left_navigation_drawer_list);
        RelativeLayout mDrawerAccountView = (RelativeLayout) findViewById(R.id.navigation_drawer_account_view);
        ImageView mChangeAccount = (ImageView) findViewById(R.id.navigation_drawer_account_information_change_account);

        mDrawerRootLayout.getLayoutParams().width = (int) calculateDrawerWidth();
        mDrawerAccountView.getLayoutParams().height = (int) calculateDrawerHeight(mDrawerRootLayout.getLayoutParams().width);

        mAdapterNavigationDrawerList = new AdapterNavigationDrawerList(mContext, mNavigationItems);
        mNavigationDrawerList.setAdapter(mAdapterNavigationDrawerList);
        mNavigationDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem  = mNavigationItems.get(position).getText();
                if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_home))){
                    showFragment(getSupportFragmentManager(), MainFr.newInstance(),R.string.navigation_drawer_item_home);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_persons))){
                    setToolbarTitle(R.string.toolbar_title_persons);
                    showFragment(getSupportFragmentManager(),PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_EDITOR, 0, null) ,R.string.navigation_drawer_item_persons);
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_journal))){
                    setToolbarTitle(R.string.toolbar_title_journal);
                    showFragment(getSupportFragmentManager(), JournalFr.newInstance(),R.string.navigation_drawer_item_journal);
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_rooms))){
                    setToolbarTitle(R.string.toolbar_title_auditrooms);
                    showFragment(getSupportFragmentManager(), RoomsFr.newInstance(),R.string.navigation_drawer_item_rooms);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_shedule))){
                    setToolbarTitle(R.string.toolbar_title_shedule);
                    //showFragment(Shedule_Fragment.newInstance(),R.string.fragment_tag_shedule);
                }else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_mail))){
                    setToolbarTitle(R.string.toolbar_title_email);
                    showFragment(getSupportFragmentManager(), EmailFr.newInstance(),R.string.navigation_drawer_item_mail);
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_sql))){
                    Dialogs dialog_sql = new Dialogs();
                    Bundle bundle_sql = new Bundle();
                    bundle_sql.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_SQL_CONNECT);
                    dialog_sql.setArguments(bundle_sql);
                    dialog_sql.show(getSupportFragmentManager(),"sql");
                }else if(selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_stat))) {
                    startActivity(new Intent(mContext, CloseDay.class).putExtra("type", 1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else if (selectedItem.equals(getStringFromResources(R.string.navigation_drawer_item_settings))){
                    startActivity(new Intent(mContext, Preferences.class));
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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            if (resultCode == RESULT_OK){
                if (requestCode == REQUEST_CODE_LOG_ON){
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result.isSuccess()) {
                        final GoogleSignInAccount acct = result.getSignInAccount();
                        AccountItem accountItem = new AccountItem().setLastname(acct.getDisplayName())
                                .setEmail(acct.getEmail())
                                .setPhoto(String.valueOf(acct.getPhotoUrl()))
                                .setAccountID(acct.getId());
                        AccountDB.writeAccount(accountItem);
                        FavoriteDB.addNewUser(new PersonItem()
                                .setLastname(acct.getDisplayName())
                                .setDivision(acct.getEmail())
                                .setRadioLabel(acct.getId()));

                        Settings.setActiveAccountID(acct.getId());
                        //initNavigationDrawer();
                    }
                }
            }

    }

    private void getUserActiveAccount(){

        AccountItem mAccount = AccountDB.getAccount(Settings.getActiveAccountID());

        if (mAccount!=null){
            String text = mAccount.getLastname();
            mAccountName.setText(text);
            mAccountEmail.setText(mAccount.getEmail());
            Picasso.with(mContext).load(mAccount.getPhoto()).into(mAccountImage);
        } else {
            mAccountName.setText(getStringFromResources(R.string.navigation_drawer_account_info_text_user));
            mAccountEmail.setText(Values.EMPTY);
            mAccountImage.setImageDrawable(null);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("connectionFAIL");
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

    public static String getStringFromResources(int resId){
        Resources resources = App.getAppContext().getResources();
        return resources.getString(resId);
    }

    private void setToolbarTitle (int resId){
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(mResources.getString(resId));
        }
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment, int fragmentTagId){

        for (int i = 0; i< mNavigationItems.size(); i++){
            mNavigationItems.get(i).setSelected(false);
        }

        for (NavigationItem navigationItem : mNavigationItems){
            if (navigationItem.getText()!=null && navigationItem.getText().equalsIgnoreCase(getStringFromResources(fragmentTagId))){
                navigationItem.setSelected(true);
            }
        }

        mAdapterNavigationDrawerList.notifyDataSetChanged();

        //если стэков больше 3, то 1 выкидываем
        if (fragmentManager.getBackStackEntryCount()>3){
            fragmentManager.popBackStack();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_for_fragment,fragment,getStringFromResources(fragmentTagId))
                .addToBackStack(getStringFromResources(fragmentTagId))
                .commit();
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

        checkUserInBase(bitmap).start();
    }

    private Thread checkUserInBase(final Bitmap bitmap){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("START check user");
                if (!FavoriteDB.isUserInBase(Settings.getActiveAccountID())){
                    System.out.println("USER NOT FOUND!!! WRITING...");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String photo = Base64.encodeToString(byteArray,Base64.NO_WRAP);
                    AccountItem account = AccountDB.getAccount(Settings.getActiveAccountID());
                    FavoriteDB.addNewUser(new PersonItem()
                            .setRadioLabel(account.getAccountID())
                            .setLastname(account.getLastname())
                            .setDivision(account.getEmail())
                            .setPhoto(photo));
                }
            }
        });
    }

    //обновление аккаунта в базе
/*    private class updateUserAccount extends AsyncTask<Bitmap, Void, Void>{

        @Override
        protected Void doInBackground(Bitmap... params) {
            System.out.println("update user account **********************");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            params[0].compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String photo = Base64.encodeToString(byteArray,Base64.NO_WRAP);

            if (FavoriteDB.isUserInBase(Settings.getActiveAccountID())){
                FavoriteDB.addNewUser(FavoriteDB.getPersonItem(Settings.getActiveAccountID(), FavoriteDB.LOCAL_USER, FavoriteDB.NO_PHOTO)
                        .setPhotoOriginal(photo)
                        .setPhotoPreview(FavoriteDB.getPhotoPreview(photo)));
            } else {
                AccountItem account = AccountDB.getAccount(Settings.getActiveAccountID());
                FavoriteDB.addNewUser(new PersonItem()
                        .setRadioLabel(account.getAccountID())
                        .setLastname(account.getLastname())
                        .setDivision(account.getEmail())
                        .setPhotoOriginal(photo)
                        .setPhotoPreview(FavoriteDB.getPhotoPreview(photo)));
            }
            System.out.println("update user account ------------------------------");
            return null;
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUME");

        //setSheduler();
        //if (mNavigationItems == null) setNavigationItems();
    }

    private void setSheduler(){
        if (mAlarm == null) mAlarm = new Alarm(App.getAppContext());
        if (Settings.getShedulerStatus()){
            if (!mAlarm.isAlarmSet()) mAlarm.setAlarm(Alarm.getClosingTime());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("DESTROY");

        if (mAlarm!=null)  mAlarm.cancelAlarm();

        //close databases
        DbShare.closeDB();

        //close reader
        mNFCReader.closeReader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("STOP");
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 1){
            super.onBackPressed();
        } else {  //если в стэке 1 фрагмент (главный)
            if (back_pressed + 2000 > System.currentTimeMillis()){
                finish();
            } else {
                Toast.makeText(getBaseContext(), getStringFromResources(R.string.toast_press_back_again), Toast.LENGTH_SHORT).show();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null){
            return false;
        } else{
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo!=null && netInfo.getState() == NetworkInfo.State.CONNECTED;
        }
    }
/*
    private class initReader extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("init reader *************************");

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

            sReaderStateChangeListener = new Reader.OnStateChangeListener() {
                @Override
                public void onStateChange(int i, int prevState, int currState) {

                    if (currState < com.acs.smartcard.Reader.CARD_UNKNOWN
                            || currState > com.acs.smartcard.Reader.CARD_SPECIFIC) {
                        currState = com.acs.smartcard.Reader.CARD_UNKNOWN;
                    }
                    final int finalCurrState = currState;
                    try {
                        if (stateStrings[finalCurrState].equals("Present")) {

                            sCardConnected = true;

                            new NFC_Reader().getTag(mReader, new ReaderInterface() {
                                @Override
                                public void onGetPersonTag(String tag) {
                                    ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                                    ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
                                    System.out.println("ACTIVITY " + componentName.getShortClassName());
                                    /*PersonsFr persons_fr = (PersonsFr) getFragmentByTag(R.string.navigation_drawer_item_persons);
                                    UserAuthFr nfc_fr = (UserAuthFr)getFragmentByTag(R.string.fragment_tag_nfc);
                                    MainFr main_fr = (MainFr) getFragmentByTag(R.string.navigation_drawer_item_home);

                                    if (tag.length() == 17){
                                        if (persons_fr != null && persons_fr.isVisible()) {

                                            new GetUser(GetUser.PERSONS).execute(tag);

                                        } else if (nfc_fr != null && nfc_fr.isVisible()) {

                                            new GetUser(GetUser.NFC).execute(tag);

                                        } else if (main_fr != null && main_fr.isVisible()) {

                                            if (RoomDB.getRoomItemForCurrentUser(tag)!=null){
                                                new CloseRooms(mContext, tag, mCloseRoomInterface).execute();
                                            }else{
                                                Toasts.showFullscreenToast(mContext, getStringFromResources(R.string.text_toast_choise_room_in_first), Toasts.TOAST_NEGATIVE);
                                            }
                                        }
                                    } else {
                                        Toasts.showFullscreenToast(mContext, getStringFromResources(R.string.text_toast_incorrect_card),Toasts.TOAST_NEGATIVE);
                                    }
                                }
                            });
                        } else {
                            sCardConnected = false;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            mReader.setOnStateChangeListener(sReaderStateChangeListener);

            System.out.println("init reader ------------------------------");
            return null;
        }
    }
*/
    @Override
    public void onSuccessBaseWrite() {
        showFragment(getSupportFragmentManager(), MainFr.newInstance(), R.string.navigation_drawer_item_home);
    }

    @Override
    public void onRoomClosed() {
        showFragment(getSupportFragmentManager(), MainFr.newInstance(), R.string.navigation_drawer_item_home);
    }

    private Fragment getFragmentByTag(int resID){
        return getSupportFragmentManager().findFragmentByTag(getResources().getString(resID));
    }


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
            System.out.println("get user ******************************");
            try{
                //если нет в базе, то добавить
                if (!FavoriteDB.isUserInBase(params[0])){
                    mValidUser = FavoriteDB.addNewUser(FavoriteDB.getPersonItem(params[0], FavoriteDB.SERVER_USER, false));

                    //если не удалось добавить, то карта некорректна. Показать тост
                    if (!mValidUser) runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasts.showFullscreenToast(App.getAppContext(), getStringFromResources(R.string.text_toast_incorrect_card), Toasts.TOAST_NEGATIVE);
                        }
                    });
                }
                return params[0];
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String personTag) {
            System.out.println("get user ---------------------------");
            if (personTag!=null && mValidUser){
                switch (mType){
                    case NFC:
                        new BaseWriter(mContext, mBaseWriterInterface).execute(new BaseWriterParams()
                                .setPersonTag(personTag)
                                .setAuditroom(Settings.getLastClickedAuditroom())
                                .setAccessType(FavoriteDB.CARD_USER_ACCESS));
                        break;
                    case PERSONS:
                        PersonsFr persons_fr = (PersonsFr) getFragmentByTag(R.string.navigation_drawer_item_persons);

                        Bundle b = new Bundle();
                        b.putInt(Dialogs.DIALOG_TYPE, Dialogs.DIALOG_EDIT);
                        b.putString(Dialogs.BUNDLE_TAG, personTag);

                        Dialogs dialog = new Dialogs();
                        dialog.setArguments(b);
                        dialog.setTargetFragment(persons_fr, 0);
                        dialog.show(persons_fr.getChildFragmentManager(), "edit");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
