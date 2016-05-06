package com.example.ivsmirnov.keyregistrator.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterNavigationDrawerList;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.DbShare;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.EmailFr;
import com.example.ivsmirnov.keyregistrator.fragments.JournalFr;
import com.example.ivsmirnov.keyregistrator.fragments.MainFr;
import com.example.ivsmirnov.keyregistrator.fragments.PersonsFr;
import com.example.ivsmirnov.keyregistrator.fragments.RoomsFr;
import com.example.ivsmirnov.keyregistrator.interfaces.CloseRoomInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.AccountDB;
import com.example.ivsmirnov.keyregistrator.fragments.Dialogs;
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
import java.io.InputStream;
import java.net.URL;


public class Launcher extends AppCompatActivity implements
        BaseWriterInterface,
        CloseRoomInterface,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener{

    public static final int REQUEST_CODE_LOG_ON = 205;

    private static final int HANDLER_ACCOUNT_WRITED = 100;

    private Context mContext;
    private Resources mResources;
    private Handler mHandler;

    //NFC reader
    private NFC mNFCReader;

    //adapters
    public static AdapterNavigationDrawerList mAdapterNavigationDrawerList;

    //navigation drawer
    //private DrawerLayout mDrawerLayout;
    //private LinearLayout mDrawerRootLayout;
    //public static ArrayList<NavigationItem> mNavigationItems;
    //private ListView mNavigationDrawerList;

    //account
    private TextView mAccountName, mAccountEmail;
    private ImageView mAccountImage, mAccountExit;
    private DrawerLayout mDrawer;

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
        setContentView(R.layout.activity_launcher);

       // ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
       /// ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
       // System.out.println("ACTIVITY " + this.getClass().getCanonicalName());

        mContext = this;
        mResources = getResources();

        //mNavigationItems = new ArrayList<>();

        //init interfaces
        mBaseWriterInterface = this;
        mCloseRoomInterface = this;

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HANDLER_ACCOUNT_WRITED:
                        initAccount();
                        break;
                    default:
                        break;
                }
            }
        };

        //connect to server
        //new SQL_Connection(Settings.getServerConnectionParams(), null).execute();
        SQL_Connection.getConnection(null);

        //init DataBases
        new DbShare();

        //init SharedPreferences
        new Settings();

        //init and set auto close alarm
        setSheduler();

        initGoogleAPI();

        //new initReader().execute();

        initUI();

        initAccount();

        if (savedInstanceState == null){
            showFragment(getSupportFragmentManager(), MainFr.newInstance(),R.string.navigation_drawer_item_home);

            mNFCReader = new NFC();
        }

    }

    private void initAccount(){
        PersonItem activePerson = FavoriteDB.getPersonItem(Settings.getActiveAccountID(), FavoriteDB.LOCAL_USER, false);
        System.out.println("active person " + activePerson);
        if (activePerson!=null){
            mAccountName.setText(activePerson.getLastname());
            mAccountEmail.setText(activePerson.getDivision());
            Picasso.with(mContext).load(FavoriteDB.getPersonPhotoPath(activePerson.getRadioLabel())).into(mAccountImage);
            mAccountExit.setVisibility(View.VISIBLE);
        }
    }

    private void initUI (){
        //элементы бокового меню
        /*if (!mNavigationItems.isEmpty()) mNavigationItems.clear();
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_home)).setIcon(R.drawable.ic_home_black_24dp).setSelected(true));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_persons)).setIcon(R.drawable.ic_person_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_journal)).setIcon(R.drawable.ic_format_list_bulleted_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_rooms)).setIcon(R.drawable.ic_room_black_24dp));
        mNavigationItems.add(new NavigationItem().setSeparator(true));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_settings)).setIcon(R.drawable.ic_settings_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_mail)).setIcon(R.drawable.ic_attachment_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_sql)).setIcon(R.drawable.ic_cloud_black_24dp));
        mNavigationItems.add(new NavigationItem().setText(mResources.getString(R.string.navigation_drawer_item_stat)).setIcon(R.drawable.ic_info_black_24dp));*/

        //тулбар
        Toolbar toolbar = (Toolbar)findViewById(R.id.layout_main_app_bar);
        setSupportActionBar(toolbar);

        //if (getSupportActionBar() != null) {
        //    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
        //    getSupportActionBar().setDisplayShowHomeEnabled(true);
        //    getSupportActionBar().setHomeButtonEnabled(true);
        //}

        //цвет статусбара начиная с lollipop
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    Window window = getWindow();
        //    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //    window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        //}

        mDrawer = (DrawerLayout)findViewById(R.id.layout_main_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_opened, R.string.navigation_drawer_closed);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)findViewById(R.id.layout_main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.navigation_item_home);

        View navHead = navigationView.getHeaderView(0);
        mAccountName = (TextView)navHead.findViewById(R.id.navigation_header_account_title);
        mAccountEmail = (TextView)navHead.findViewById(R.id.navigation_header_account_subtitle);
        mAccountImage = (ImageView)navHead.findViewById(R.id.navigation_header_account_image);
        mAccountExit = (ImageView)navHead.findViewById(R.id.navigation_header_exit);

        mAccountImage.setOnClickListener(logOnClick);
        mAccountExit.setOnClickListener(logOutClick);

        //getUserActiveAccount();

/*
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

        ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, app_bar_main, R.string.navigation_drawer_opened, R.string.navigation_drawer_closed) {
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

        getUserActiveAccount();*/
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.navigation_item_home:
                showFragment(getSupportFragmentManager(), MainFr.newInstance(),R.string.toolbar_title_main);
                break;
            case R.id.navigation_item_persons:
                setToolbarTitle(R.string.toolbar_title_persons);
                showFragment(getSupportFragmentManager(),PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_EDITOR, 0, null) ,R.string.toolbar_title_persons);
                break;
            case R.id.navigation_item_journal:
                showFragment(getSupportFragmentManager(), JournalFr.newInstance(),R.string.toolbar_title_journal);
                break;
            case R.id.navigation_item_rooms:
                showFragment(getSupportFragmentManager(), RoomsFr.newInstance(),R.string.toolbar_title_auditrooms);
                break;
            case R.id.navigation_item_settings:
                startActivity(new Intent(mContext, Preferences.class));
                break;
            case R.id.navigation_item_email:
                showFragment(getSupportFragmentManager(), EmailFr.newInstance(),R.string.navigation_drawer_item_mail);
                break;
            case R.id.navigation_item_sql_connect:
                break;
            case R.id.navigation_item_download_all_from_server:
                new ServerReader(mContext, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerReader.LOAD_ROOMS);
                new ServerReader(mContext, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerReader.LOAD_TEACHERS);
                new ServerReader(mContext, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerReader.LOAD_JOURNAL);
                break;
            case R.id.navigation_item_upload_all_to_server:
                new ServerWriter(mContext, true).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.ROOMS_UPDATE);
                new ServerWriter(mContext, true).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.PERSON_UPDATE);
                new ServerWriter(mContext, true).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ServerWriter.JOURNAL_UPDATE);
                break;
            default:
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);

        return true;
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
            showLogOutDialog();
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
                       // final GoogleSignInAccount acct = result.getSignInAccount();
                        //AccountItem accountItem = new AccountItem().setLastname(acct.getDisplayName())
                        //        .setEmail(acct.getEmail())
                        //        .setPhoto(String.valueOf(acct.getPhotoUrl()))
                        //        .setAccountID(acct.getId());
                        //AccountDB.writeAccount(accountItem);
                        //FavoriteDB.addNewUser(new PersonItem()
                        //        .setLastname(acct.getDisplayName())
                        //        .setDivision(acct.getEmail())
                        //        .setRadioLabel(acct.getId()));

                        //mAccountName.setText(acct.getDisplayName());
                        //mAccountEmail.setText(acct.getEmail());
                        //mAccountExit.setVisibility(View.VISIBLE);
                        //Picasso.with(mContext).load(acct.getPhotoUrl()).into(mAccountImage);

                        //initNavigationDrawer();

                        writeAccountInLocal(result.getSignInAccount()).start();
                    }
                }
            } else {
                Settings.setActiveAccountID(getString(R.string.local_account));
            }
    }

    private Thread writeAccountInLocal(final GoogleSignInAccount account){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(String.valueOf(account.getPhotoUrl())).openConnection().getInputStream());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                    String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
                    System.out.println("Encoded image " + encoded);

                    FavoriteDB.addNewUser(new PersonItem()
                            .setLastname(account.getDisplayName())
                            .setDivision(account.getEmail())
                            .setRadioLabel(account.getId())
                            .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                            .setPhoto(encoded));

                    Settings.setActiveAccountID(account.getId());

                    mHandler.sendEmptyMessage(HANDLER_ACCOUNT_WRITED);
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("connectionFAIL");
    }


    public static String getStringFromResources(int resId){
        Resources resources = App.getAppContext().getResources();
        return resources.getString(resId);
    }

    public void setToolbarTitle (int resId){
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(getResources().getString(resId));
        }
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment, int fragmentTagId){

        //for (int i = 0; i< mNavigationItems.size(); i++){
        //    mNavigationItems.get(i).setSelected(false);
        //}

        //for (NavigationItem navigationItem : mNavigationItems){
        //    if (navigationItem.getText()!=null && navigationItem.getText().equalsIgnoreCase(getStringFromResources(fragmentTagId))){
        //        navigationItem.setSelected(true);
        //    }
        //}

        //mAdapterNavigationDrawerList.notifyDataSetChanged();

        //если стэков больше 3, то 1 выкидываем
        //if (fragmentManager.getBackStackEntryCount()>3){
        //    fragmentManager.popBackStack();
        //}

        fragmentManager.beginTransaction()
                .replace(R.id.layout_main_content_frame,fragment,getStringFromResources(fragmentTagId))
                //.addToBackStack(getStringFromResources(fragmentTagId))
                .commit();
    }

    //@Override
    //public void onUserRecoverableAuthException(UserRecoverableAuthException e) {
    //    startActivityForResult(e.getIntent(),123);
    //}

    private void showLogOutDialog(){
        new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.title_dialog_log_out))
                .setMessage(getString(R.string.dialog_log_out_message))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                        Settings.setActiveAccountID(getResources().getString(R.string.local_account));

                        mAccountName.setText(getString(R.string.local_account));
                        mAccountEmail.setText("");
                        mAccountImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_48dp));
                        mAccountExit.setVisibility(View.INVISIBLE);
                       // getUserActiveAccount();
                    }
                })
                .create()
                .show();
    }

/*
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
   private class updateUserAccount extends AsyncTask<Bitmap, Void, Void>{

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
        if (mNFCReader!=null){
            mNFCReader.closeReader();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("STOP");
    }

    @Override
    public void onBackPressed() {
        if (mDrawer!=null && mDrawer.isDrawerOpen(GravityCompat.START)){
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()){
                finish();
            } else {
                Toast.makeText(getBaseContext(), getStringFromResources(R.string.toast_press_back_again), Toast.LENGTH_SHORT).show();
                back_pressed = System.currentTimeMillis();
            }
        }
    }
    /*
        public static boolean isNetworkAvailable() {
            ConnectivityManager cm = (ConnectivityManager)App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null){
                return false;
            } else{
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo!=null && netInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }

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
