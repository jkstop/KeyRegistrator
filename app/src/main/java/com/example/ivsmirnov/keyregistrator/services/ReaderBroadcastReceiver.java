package com.example.ivsmirnov.keyregistrator.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.async_tasks.Open_Reader;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 02.02.2016.
 */
public class ReaderBroadcastReceiver {

    public static BroadcastReceiver getReaderReceiver(final Reader reader){
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Values.ACTION_USB_PERMISSION.equals(intent.getAction())){
                    synchronized (this){
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
                            if (device!=null){
                                Log.d("opening","reader...");
                                new Open_Reader(reader).execute();
                            }
                        }
                    }
                }
            }
        };
    }
}
