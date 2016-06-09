package com.example.ivsmirnov.keyregistrator.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.KeyEvent;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.activities.Preferences;
import com.example.ivsmirnov.keyregistrator.activities.UserAuth;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.sql.Connection;

/**
 * NFC считыватель

public class NFC implements
        SQL_Connection.Callback,
        ServerReader.Callback,
BaseWriter.Callback{

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int RECEIVER_REQUEST_CODE = 100;
    private Reader mReader;
    private Context mContext;
    private String mCurrentRadioLabel;

    private SQL_Connection.Callback mConnectionCallback;
    private ServerReader.Callback mServerReaderCallback;
    private BaseWriter.Callback mBaseWriterCallback;
    private Callback mCallback;

    public NFC(Callback callback) {
        mContext = App.getAppContext();

        mConnectionCallback = this;
        mServerReaderCallback = this;
        mBaseWriterCallback = this;
        mCallback = callback;

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

                ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                String runnungActivityName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();

                if (runnungActivityName.equals(Launcher.class.getCanonicalName())){
                    System.out.println("activity_launcher");
                } else if (runnungActivityName.equals(Preferences.class.getCanonicalName())){
                    System.out.println("settings");
                } else if (runnungActivityName.equals(CloseDay.class.getCanonicalName())){
                    System.out.println("close day");
                } else if (runnungActivityName.equals(UserAuth.class.getCanonicalName())){
                    System.out.println("user auth");
                    if (!FavoriteDB.isUserInBase(mCurrentRadioLabel)){
                        System.out.println("user not in base");
                        SQL_Connection.getConnection(null, 0, mConnectionCallback);
                    } else {
                        System.out.println("user in base");
                        write(new PersonItem()
                                .setRadioLabel(mCurrentRadioLabel)
                                .setAccessType(FavoriteDB.CARD_USER_ACCESS));
                    }
                } else{
                    System.out.println("hm. WTF?");
                }
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
            if (ACTION_USB_PERMISSION.equals(intent.getAction())){
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

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        System.out.println("server connected");
        new ServerReader(ServerReader.READ_PERSON_ITEM, mCurrentRadioLabel, mServerReaderCallback).execute(connection);
    }

    @Override
    public void onServerConnectException(Exception e) {

    }

    private void write(PersonItem personItem){
        new BaseWriter(BaseWriter.WRITE_NEW, mContext, mBaseWriterCallback)
                .execute(new BaseWriterParams()
                        .setPersonTag(personItem.getRadioLabel())
                        .setAccessType(personItem.getAccessType())
                        .setAuditroom(UserAuth.mSelectedRoom));
    }

    @Override
    public void onSuccessServerRead(Object result) {
        System.out.println("success server read");
        if (result!=null){
            PersonItem personItem = (PersonItem)result;
            if (FavoriteDB.addNewUser(personItem, Settings.getWriteServerStatus())){
                write(personItem);
            }
        }
    }

    @Override
    public void onErrorServerRead(Exception e) {
        Toasts.handler.sendEmptyMessage(Toasts.TOAST_WRONG_CARD);
    }

    @Override
    public void onSuccessBaseWrite() {
        System.out.println("success base write");
        mCallback.onSuccessBaseWrite();
    }

    @Override
    public void onErrorBaseWrite() {
        mCallback.onErrorBaseWrite();
    }

    public interface Callback{
        void onSuccessBaseWrite();
        void onErrorBaseWrite();
    }

}
 */