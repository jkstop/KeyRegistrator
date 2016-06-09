package com.example.ivsmirnov.keyregistrator.activities;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.ivsmirnov.keyregistrator.fragments.DialogUserAuth;
import com.example.ivsmirnov.keyregistrator.fragments.JournalFr;
import com.example.ivsmirnov.keyregistrator.fragments.MainFr;
import com.example.ivsmirnov.keyregistrator.fragments.PersonsFr;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.services.Alarm;
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
import java.util.ArrayList;
import java.util.List;


public class Launcher extends AppCompatActivity implements
ServerWriter.Callback,
ServerReader.Callback,
SQL_Connection.Callback,
        BaseWriter.Callback,
        DialogUserAuth.Callback,
//NFC.Callback,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener{

    public static final int REQUEST_CODE_LOG_ON = 205;

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

    private GoogleApiClient mGoogleApiClient;

    private static long back_pressed;

    private Reader mReader;
    public static Reader.OnStateChangeListener sReaderStateChangeListener;
    public static boolean sCardConnected = false;
    public String mCurrentRadioLabel;

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

        //init and set auto close alarm
        setSheduler();

        initUI();

        initGoogleAPI();

        initAccount();

        if (savedInstanceState == null){
            //connect to server
            SQL_Connection.getConnection(null, 0, mSQLConnectCallback);

            //init DataBases
            //new DbShare();

            //init SharedPreferences
            //new Settings();


            showFragment(getSupportFragmentManager(), MainFr.newInstance(),R.string.toolbar_title_main);
            mNFCReader = new NFC_reader();
        }

    }

    private void initAccount(){
        PersonItem activePerson = FavoriteDB.getPersonItem(Settings.getActiveAccountID(), false);
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
                showFragment(getSupportFragmentManager(), MainFr.newInstance(),R.string.toolbar_title_main);
                break;
            case R.id.navigation_item_persons:
                showFragment(getSupportFragmentManager(),PersonsFr.newInstance(PersonsFr.PERSONS_FRAGMENT_EDITOR, 0, null) ,R.string.toolbar_title_persons);
                break;
            case R.id.navigation_item_journal:
                showFragment(getSupportFragmentManager(), JournalFr.newInstance(),R.string.toolbar_title_journal);
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
                            .setPhoto(encoded),
                            Settings.getWriteServerStatus());

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
        fragmentManager.beginTransaction()
                .replace(R.id.layout_main_content_frame,fragment,getStringFromResources(fragmentTagId))
                .commit();
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUME");
        System.out.println("IS ALARM SET " + Alarm.isAlarmSet());
    }

    private void setSheduler(){
        System.out.println("sheduler status " + Settings.getShedulerStatus());
        if (Settings.getShedulerStatus()){
            System.out.println("alarm is set? " + Alarm.isAlarmSet());

            Alarm.setAlarm(Alarm.getClosingTime(null));
            System.out.println("done");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("DESTROY");

        Alarm.cancelAlarm();

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

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        System.out.println("CONNECTED : " + connection);
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
        Snackbar.make(mContentFrame,"Нет соединения с сервером",Snackbar.LENGTH_SHORT)
                .setAction("Настройки", new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        startActivity(new Intent(mContext, Preferences.class));
                    }
                })
                .show();
    }

    @Override
    public void onSuccessServerWrite() {
        Snackbar.make(mContentFrame,"Запись прошла успешно",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onErrorServerWrite() {
        Snackbar.make(mContentFrame,"Ошибка при записи",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessServerRead(Object result) {
        Snackbar.make(mContentFrame,"Чтение успешно",Snackbar.LENGTH_SHORT).show();
        if (result!=null){
            PersonItem personItem = (PersonItem)result;
            if (FavoriteDB.addNewUser(personItem, Settings.getWriteServerStatus())){
                write(personItem);
            }
        }
    }

    @Override
    public void onErrorServerRead(Exception e) {
        Snackbar.make(mContentFrame,"Ошибка при чтении",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessBaseWrite() {

        System.out.println("success base write 111");
        MainFr.updateGrid();
    }

    @Override
    public void onErrorBaseWrite() {
        System.out.println("error base write");
    }

    private void write(PersonItem personItem){
        new BaseWriter(BaseWriter.WRITE_NEW, mContext, this)
                .execute(new BaseWriterParams()
                        .setPersonTag(personItem.getRadioLabel())
                        .setAccessType(personItem.getAccessType())
                        .setAuditroom(UserAuth.mSelectedRoom));
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
            mReader.setOnStateChangeListener(readerStateChangeListener);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, RECEIVER_REQUEST_CODE, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);

            mContext.registerReceiver(mReceiver, filter);

            System.out.println("DEVICES " + manager.getDeviceList().values().size());
            for (UsbDevice device : manager.getDeviceList().values()) {
                manager.requestPermission(device,pendingIntent);
            }
        }

        Reader.OnStateChangeListener readerStateChangeListener = new Reader.OnStateChangeListener(){
            @Override
            public void onStateChange(int i, int previousState, int currentState) {
                if (currentState < Reader.CARD_UNKNOWN || currentState > Reader.CARD_SPECIFIC) currentState = Reader.CARD_UNKNOWN;

                if (currentState == Reader.CARD_PRESENT){
                    mCurrentRadioLabel = getRadioLabelValue();
                    System.out.println("CARD CONNECTED " + mCurrentRadioLabel);

                    DialogUserAuth dialogUserAuth = (DialogUserAuth)getSupportFragmentManager().findFragmentByTag(getString(R.string.title_activity_user_auth));

                    if (dialogUserAuth!=null && dialogUserAuth.isVisible()){
                        System.out.println("AUTH VISIBLE");
                    }

                   // System.out.println("dialog " + dialogUserAuth);
                    //if (dialogUserAuth!=null){
                    //
                   // }
                    //ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    //String runnungActivityName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();

//                    if (runnungActivityName.equals(Launcher.class.getCanonicalName())){
 //                       System.out.println("activity_launcher");
 //                   } else if (runnungActivityName.equals(Preferences.class.getCanonicalName())){
 //                       System.out.println("settings");
  //                  } else if (runnungActivityName.equals(CloseDay.class.getCanonicalName())){
  //                      System.out.println("close day");
   //                 } else if (runnungActivityName.equals(UserAuth.class.getCanonicalName())){
    //                    System.out.println("user auth");
    //                    if (!FavoriteDB.isUserInBase(mCurrentRadioLabel)){
     //                       System.out.println("user not in base");
      //                      SQL_Connection.getConnection(null, ServerReader.READ_PERSON_ITEM, mSQLConnectCallback);
       //                 } else {
       //                     System.out.println("user in base");
        //                    write(new PersonItem()
        //                            .setRadioLabel(mCurrentRadioLabel)
         //                           .setAccessType(FavoriteDB.CARD_USER_ACCESS));
          //              }
           //         } else{
          //              System.out.println("hm. WTF?");
          //          }
              }

                if (currentState == Reader.CARD_ABSENT){
                    System.out.println("CARD DISCONNECTED");
                }
            }
        };

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
                        System.out.println("OPENED READER " + mReader.getReaderName());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public void closeReader(){
            if (mReader!=null && mContext!=null){
                System.out.println("UNREGISTER READER " + mReader.getReaderName() + " WITH RECEIVER " + mReceiver.toString());
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
