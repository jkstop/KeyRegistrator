package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;

/**
 * Открываем считыватель
 */
public class Open_Reader extends AsyncTask<UsbDevice,Void,Exception> {

    private Reader mReader;

    public Open_Reader(Reader reader){
        this.mReader = reader;
    }

    @Override
    protected Exception doInBackground(UsbDevice... params) {
        Exception result = null;
        try{
            mReader.open(params[0]);
        }catch (Exception e){
            result = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Exception e) {
        if (e!=null){
            Log.d("Exception", e.toString());
        }else{
            Log.d("Reader name",mReader.getReaderName());
        }
    }
}
