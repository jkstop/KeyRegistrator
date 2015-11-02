package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;

/**
 * Created by ivsmirnov on 02.11.2015.
 */
public class Protocol_Reader extends AsyncTask<Launcher.SetProtocolParams,Void,Launcher.SetProtocolResult> {

    private Reader mReader;
    public Protocol_Reader (Reader reader){
        this.mReader = reader;
    }

    @Override
    protected Launcher.SetProtocolResult doInBackground(Launcher.SetProtocolParams... params) {
        Launcher.SetProtocolResult result = new Launcher.SetProtocolResult();
        try{
            result.activeProtocol = mReader.setProtocol(params[0].slotNum, params[0].preferredProtocols);
        }catch (Exception e){
            result.e = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Launcher.SetProtocolResult setProtocolResult) {
        if (setProtocolResult.e!=null){
            Log.d("Exeption", setProtocolResult.e.toString());
        }else{
            String activeProtocolString = "ActiveProtocol: ";
            switch (setProtocolResult.activeProtocol) {

                case Reader.PROTOCOL_T0:
                    activeProtocolString += "T=0";
                    break;

                case Reader.PROTOCOL_T1:
                    activeProtocolString += "T=1";
                    break;

                default:
                    activeProtocolString += "Unknown";
                    break;
            }
            Log.d("Protocol:",activeProtocolString);
        }
    }
}
