package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;

/**
 * Created by ivsmirnov on 02.11.2015.
 */
public class Power_Reader extends AsyncTask<Launcher.PowerParams,Void,Launcher.PowerResult>{

    private Reader mReader;

    public Power_Reader (Reader reader){
        this.mReader = reader;
    }
    @Override
    protected Launcher.PowerResult doInBackground(Launcher.PowerParams... params) {
        Launcher.PowerResult result= new Launcher.PowerResult();
        try {
            result.atr = mReader.power(params[0].slotNum,params[0].action);
        }catch (Exception e){
            result.e = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Launcher.PowerResult powerResult) {
        if (powerResult.e!=null){
            Log.d("Exception", powerResult.toString());
        }else{
            if (powerResult.atr!=null){
                Log.d("atr", getStringFromByte(powerResult.atr,powerResult.atr.length));
            }
        }
    }

    private String getStringFromByte(byte[] buffer, int bufferLength) {
        String bufferString = "";
        for (int i = 0; i < bufferLength; i++) {
            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }
            if (i % 20 == 0) {

                if (bufferString != "") {
                    bufferString = "";
                }
            }
            bufferString += hexChar.toUpperCase() + " ";
        }
        return bufferString;
    }
}
