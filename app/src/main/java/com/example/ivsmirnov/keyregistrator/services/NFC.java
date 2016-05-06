package com.example.ivsmirnov.keyregistrator.services;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.activities.CloseDay;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.activities.Preferences;
import com.example.ivsmirnov.keyregistrator.activities.UserAuth;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;

/**
 * NFC считыватель
 */
public class NFC {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int RECEIVER_REQUEST_CODE = 100;
    private Reader mReader;
    private Context mContext;

    public NFC() {
        mContext = App.getAppContext();
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
                System.out.println("CARD CONNECTED " + getRadioLabelValue());
                ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                String runnungActivityName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                System.out.println(runnungActivityName);
                if (runnungActivityName.equals(Launcher.class.getCanonicalName())){
                    System.out.println("activity_launcher");
                } else if (runnungActivityName.equals(Preferences.class.getCanonicalName())){
                    System.out.println("settings");
                } else if (runnungActivityName.equals(CloseDay.class.getCanonicalName())){
                    System.out.println("close day");
                } else if (runnungActivityName.equals(UserAuth.class.getCanonicalName())){
                    System.out.println("user auth");
                } else{
                    System.out.println("hm. WTF?");
                }
                //switch (componentName.getClassName()){
                //    case Launcher.class.getCanonicalName():
                //        System.out.println("activity_launcher");
                //        break;
                //    default:
                //        break;

                //}
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

    public interface Callback{
        void onGetRadioLabelFromReader(String radioLabel);
    }

}
