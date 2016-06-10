package com.example.ivsmirnov.keyregistrator.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.DbShare;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.fragments.DialogUserAuth;
import com.example.ivsmirnov.keyregistrator.fragments.Journal;
import com.example.ivsmirnov.keyregistrator.fragments.Rooms;
import com.example.ivsmirnov.keyregistrator.fragments.Users;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.example.ivsmirnov.keyregistrator.services.Alarm;
import com.example.ivsmirnov.keyregistrator.services.Toasts;
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
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Launcher extends AppCompatActivity implements
ServerWriter.Callback,
ServerReader.Callback,
SQL_Connection.Callback,
        BaseWriter.Callback,
        DialogUserAuth.Callback,
//NFC.Callback,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_CODE_LOG_ON = 205;

    private static final int HANDLER_ACCOUNT_WRITED = 100;

    private Context mContext;
    private Resources mResources;
    private Handler mHandler;

    //NFC reader
    private NFC_reader mNFCReader;

    //account
    private TextView mAccountName, mAccountEmail;
    private ImageView mAccountImage, mAccountExit;
    private DrawerLayout mDrawer;

    private ServerWriter.Callback mServerWriteCallback;
    private SQL_Connection.Callback mSQLConnectCallback;
    private ServerReader.Callback mServerReaderCallback;
    private BaseWriter.Callback mBaseWriterCallback;

    private GoogleApiClient mGoogleApiClient;

    private static long back_pressed;

    private Reader mReader;
    public static Reader.OnStateChangeListener sReaderStateChangeListener;
    public static boolean sCardConnected = false;
    public static boolean sDirectWrite = false;
    private String mCurrentRadioLabel;

    private static FrameLayout mContentFrame;


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

        mContext = this;
        mResources = getResources();

        //init interfaces

        mServerWriteCallback = this;
        mServerReaderCallback = this;
        mSQLConnectCallback = this;
        mBaseWriterCallback = this;

        mHandler = new Handler(Looper.getMainLooper()){
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

        //init and set auto close alarm
        setSheduler();

        initUI();

        initGoogleAPI();

        initAccount();

        mNFCReader = new NFC_reader();

        if (savedInstanceState == null){
            //connect to server
            SQL_Connection.getConnection(null, 0, mSQLConnectCallback);

            //init DataBases
            //new DbShare();

            //init SharedPrefs
            //new SharedPrefs();


            showFragment(getSupportFragmentManager(), Rooms.newInstance(),R.string.title_rooms_loading);

        }

    }

    private void initAccount(){
        PersonItem activePerson = FavoriteDB.getPersonItem(SharedPrefs.getActiveAccountID(), false);
        System.out.println("active person " + activePerson);
        if (activePerson!=null){
            mAccountName.setText(activePerson.getLastname());
            mAccountEmail.setText(activePerson.getDivision());
            Picasso.with(mContext).load(FavoriteDB.getPersonPhotoPath(activePerson.getRadioLabel())).into(mAccountImage);
            mAccountExit.setVisibility(View.VISIBLE);
        }
    }

    private void initUI (){
        //главный контейнер для фрагментов
        mContentFrame = (FrameLayout) findViewById(R.id.layout_main_content_frame);

        //тулбар
        Toolbar toolbar = (Toolbar)findViewById(R.id.layout_main_app_bar);
        setSupportActionBar(toolbar);

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
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.navigation_item_home:
                showFragment(getSupportFragmentManager(), Rooms.newInstance(),R.string.title_rooms_loading);
                break;
            case R.id.navigation_item_persons:
                showFragment(getSupportFragmentManager(), Users.newInstance(Users.PERSONS_FRAGMENT_EDITOR, 0, null) ,R.string.title_users);
                break;
            case R.id.navigation_item_journal:
                showFragment(getSupportFragmentManager(), Journal.newInstance(),R.string.title_journal);
                break;
            case R.id.navigation_item_settings:
                startActivity(new Intent(mContext, Preferences.class));
                break;
            case R.id.navigation_item_stat:
                startActivity(new Intent(mContext, CloseDay.class)
                        .putExtra(CloseDay.TITLE, CloseDay.STAT_TITLE)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.navigation_item_download_all_from_server:
                SQL_Connection.getConnection(null, ServerReader.READ_ALL, mSQLConnectCallback);
                break;
            case R.id.navigation_item_upload_all_to_server:
                SQL_Connection.getConnection(null, ServerWriter.UPDATE_ALL, mSQLConnectCallback);
                break;
            default:
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);

        return true;
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

    private View.OnClickListener logOutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showLogOutDialog();
        }
    };

    private View.OnClickListener logOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, REQUEST_CODE_LOG_ON);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        System.out.println("result logon " + resultCode);
            if (resultCode == RESULT_OK){
                if (requestCode == REQUEST_CODE_LOG_ON){
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result.isSuccess()) {
                        writeAccountInLocal(result.getSignInAccount()).start();
                    }
                }
            } else {
                SharedPrefs.setActiveAccountEmail(null);
                SharedPrefs.setActiveAccountID(getString(R.string.local_account));
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

                    FavoriteDB.addNewUser(new PersonItem()
                            .setLastname(account.getDisplayName())
                            .setDivision(account.getEmail())
                            .setRadioLabel(account.getId())
                            .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                            .setPhoto(encoded),
                            SharedPrefs.getWriteServerStatus());

                    SharedPrefs.setActiveAccountID(account.getId());
                    SharedPrefs.setActiveAccountEmail(account.getEmail());

                    mHandler.sendEmptyMessage(HANDLER_ACCOUNT_WRITED);
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("no connect");
    }

    public void setToolbarTitle (int resId){
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(getResources().getString(resId));
        }
    }

    private static void showFragment(FragmentManager fragmentManager, Fragment fragment, int fragmentTagId){
        fragmentManager.beginTransaction()
                .replace(R.id.layout_main_content_frame,fragment, App.getAppContext().getString(fragmentTagId))
                .commit();
    }

    private void showLogOutDialog(){
        new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.title_log_out))
                .setMessage(getString(R.string.log_out_disclaimer))
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
                        SharedPrefs.setActiveAccountID(getResources().getString(R.string.local_account));

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



    private void setSheduler(){
        if (SharedPrefs.getShedulerStatus()){
            Alarm.setAlarm(Alarm.getClosingTime(null));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Alarm.cancelAlarm();

        //close databases
        DbShare.closeDB();

        //close reader
        if (mNFCReader!=null){
            mNFCReader.closeReader();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer!=null && mDrawer.isDrawerOpen(GravityCompat.START)){
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()){
                finish();
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.toast_press_back_again), Toast.LENGTH_SHORT).show();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        switch (callingTask){
            case ServerWriter.UPDATE_ALL:
                new ServerWriter(ServerWriter.UPDATE_ALL, null, false, mServerWriteCallback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                break;
            case ServerReader.READ_ALL:
                new ServerReader(ServerReader.READ_ALL, mContext, mServerReaderCallback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                break;
            case ServerReader.READ_PERSON_ITEM:
                new ServerReader(ServerReader.READ_PERSON_ITEM, mCurrentRadioLabel, mServerReaderCallback).execute(connection);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerConnectException(Exception e) {
        Snackbar.make(mContentFrame,getString(R.string.snack_server_connect_error),Snackbar.LENGTH_SHORT)
                .setAction(getString(R.string.title_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        startActivity(new Intent(mContext, Preferences.class));
                    }
                })
                .show();
    }

    @Override
    public void onSuccessServerWrite() {
        Snackbar.make(mContentFrame,getString(R.string.snack_server_write_success),Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onErrorServerWrite() {
        Snackbar.make(mContentFrame,getString(R.string.snack_server_write_error),Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessServerRead(int task, Object result) {
        Snackbar.make(mContentFrame,getString(R.string.snack_server_read_success),Snackbar.LENGTH_SHORT).show();
        switch (task){
            case ServerReader.READ_PERSON_ITEM:
                if (result!=null){
                    PersonItem personItem = (PersonItem)result;
                    if (FavoriteDB.addNewUser(personItem, SharedPrefs.getWriteServerStatus())){
                        write(personItem);
                    }
                } else {
                    Toasts.handler.sendEmptyMessage(Toasts.TOAST_WRONG_CARD);
                }
                break;
            case ServerReader.READ_ALL:
                Rooms rooms = (Rooms) getSupportFragmentManager().findFragmentByTag(getString(R.string.title_rooms_loading));
                Users users = (Users) getSupportFragmentManager().findFragmentByTag(getString(R.string.title_users));
                Journal journal = (Journal) getSupportFragmentManager().findFragmentByTag(getString(R.string.title_journal));
                if (rooms!=null && rooms.isVisible()){
                    Rooms.updateGrid();
                } else if (users!=null && users.isVisible()){
                    users.initPersons("#", true).start();
                } else if (journal!=null && journal.isVisible()){
                    journal.getJournal(new Date(System.currentTimeMillis())).start();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorServerRead(Exception e) {
        Snackbar.make(mContentFrame,getString(R.string.snack_server_read_error),Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessBaseWrite() {

        DialogUserAuth dialogUserAuth = (DialogUserAuth)getSupportFragmentManager().findFragmentByTag(getString(R.string.title_user_auth));
        if (dialogUserAuth!=null && dialogUserAuth.isVisible()){
            dialogUserAuth.getDialog().cancel();
        }
        Rooms.updateGrid();
    }

    @Override
    public void onErrorBaseWrite() {
    }

    private void write(PersonItem personItem){
        new BaseWriter(BaseWriter.WRITE_NEW, mContext, mBaseWriterCallback)
                .execute(new BaseWriterParams()
                        .setPersonTag(personItem.getRadioLabel())
                        .setAccessType(personItem.getAccessType())
                        .setAuditroom(DialogUserAuth.mSelectedRoom));
    }

    public class NFC_reader{
        private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
        private static final int RECEIVER_REQUEST_CODE = 100;
        private Reader mReader;
        private Context mContext;

        public NFC_reader(){
            mContext = getApplicationContext();
            UsbManager manager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
            mReader = new Reader(manager);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, RECEIVER_REQUEST_CODE, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);

            mContext.registerReceiver(mReceiver, filter);

            for (UsbDevice device : manager.getDeviceList().values()) {
                manager.requestPermission(device,pendingIntent);
            }

            sReaderStateChangeListener = new Reader.OnStateChangeListener(){
                @Override
                public void onStateChange(int i, int previousState, int currentState) {
                    if (currentState < Reader.CARD_UNKNOWN || currentState > Reader.CARD_SPECIFIC) currentState = Reader.CARD_UNKNOWN;

                    if (currentState == Reader.CARD_PRESENT){
                        sCardConnected = true;
                        mCurrentRadioLabel = getRadioLabelValue();

                        DialogUserAuth dialogUserAuth = (DialogUserAuth)getSupportFragmentManager().findFragmentByTag(getString(R.string.title_user_auth));
                        Rooms rooms = (Rooms) getSupportFragmentManager().findFragmentByTag(getString(R.string.title_rooms_loading));

                        if (sDirectWrite){
                            write(FavoriteDB.getPersonItem(mCurrentRadioLabel, false));
                            sDirectWrite = false;
                        }else if (dialogUserAuth!=null){
                            if (!FavoriteDB.isUserInBase(mCurrentRadioLabel)){
                                SQL_Connection.getConnection(null, ServerReader.READ_PERSON_ITEM, mSQLConnectCallback);
                            } else {
                                write(FavoriteDB.getPersonItem(mCurrentRadioLabel, false));
                            }

                        } else if (rooms !=null && rooms.isVisible()){
                            if (RoomDB.getRoomItemForCurrentUser(mCurrentRadioLabel)!=null){
                                new BaseWriter(BaseWriter.UPDATE_CURRENT, mContext, mBaseWriterCallback)
                                        .execute(new BaseWriterParams()
                                                .setPersonTag(mCurrentRadioLabel));
                            } else {
                                if (FavoriteDB.isUserInBase(mCurrentRadioLabel)){
                                    Toasts.handler.sendEmptyMessage(Toasts.TOAST_SELECT_ROOM_FIRST);
                                } else {
                                    Toasts.handler.sendEmptyMessage(Toasts.TOAST_WRONG_CARD);
                                }

                            }
                        }
                    }

                    if (currentState == Reader.CARD_ABSENT){
                        sCardConnected = false;
                    }
                }
            };

            mReader.setOnStateChangeListener(sReaderStateChangeListener);
        }


        private String getRadioLabelValue(){
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T1 );
                byte [] transmitCommand = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04};
                byte [] response = new byte[100];
                int responseLength = mReader.transmit(0,
                        transmitCommand,
                        transmitCommand.length,
                        response,
                        response.length);
                return getStringFromByte(response, responseLength);
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        private void openReader (final UsbDevice usbDevice){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mReader.open(usbDevice);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public void closeReader(){
            if (mReader!=null && mContext!=null){
                mReader.close();
                mContext.unregisterReceiver(mReceiver);
            }
        }

        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NFC_reader.ACTION_USB_PERMISSION.equals(intent.getAction())){
                    synchronized (this){
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                            if (device!=null){
                                openReader(device);
                            }
                        }
                    }
                }
            }
        };

        private String getStringFromByte(byte[] buffer, int bufferLength) {
            String bufferString = "";
            for (int i = 0; i < bufferLength - 2; i++) {
                String hexChar = Integer.toHexString(buffer[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                if (i % 20 == 0) {
                    if (!bufferString.equals("")) {
                        bufferString = "";
                    }
                }
                bufferString += hexChar.toUpperCase() + " ";
            }
            return bufferString + "00 00";
        }
    }



}
