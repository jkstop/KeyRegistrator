package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;

/**
 * Created by ivsmirnov on 02.11.2015.
 */
public class Tag_Reader extends AsyncTask <Launcher.TransmitParams,Void,Launcher.TransmitResult> {

    private Reader mReader;
    private Context mContext;

    public Tag_Reader (Context context,Reader reader){
        this.mContext = context;
        this.mReader = reader;
    }
    @Override
    protected Launcher.TransmitResult doInBackground(Launcher.TransmitParams... params) {
        Launcher.TransmitResult result = new Launcher.TransmitResult();
        byte[] command = params[0].command;
        byte[] response = null;

        response = new byte[100];
        try {
            result.responseLength = mReader.transmit(params[0].slotNum,
                    command, command.length, response,
                    response.length);
            result.response = response;
        } catch (Exception e) {
            result.e = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Launcher.TransmitResult transmitResult) {
        if (transmitResult.e!=null){
            Log.d("Exception", transmitResult.e.toString());
        }else{
            String tag = getStringFromByte(transmitResult.response,transmitResult.responseLength-2);
            Toast.makeText(mContext, tag + "00 00", Toast.LENGTH_SHORT).show();
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
