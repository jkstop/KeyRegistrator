package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseStaff;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Nfc_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.GetUserByTag;

/**
 * Created by ivsmirnov on 02.11.2015.
 */
public class Tag_Reader extends AsyncTask <Launcher.TransmitParams,Void,Launcher.TransmitResult> {

    private Reader mReader;
    private Context mContext;
    private GetUserByTag mListener;
    private ProgressDialog progressDialog;


    public Tag_Reader (Context context,Reader reader,GetUserByTag l){
        this.mContext = context;
        this.mReader = reader;
        this.mListener = l;
        progressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Подождите, идет загрузка...");
        progressDialog.show();
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

            DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
            SparseArray<String> items = dbFavorite.findUserByTag(tag + "00 00");
            dbFavorite.closeDB();

            if (progressDialog.isShowing()){
                progressDialog.cancel();
            }

            mListener.onGetSparse(items);
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
