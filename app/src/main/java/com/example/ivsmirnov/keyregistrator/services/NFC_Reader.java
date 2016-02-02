package com.example.ivsmirnov.keyregistrator.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.async_tasks.Open_Reader;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Nfc_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.ReaderResponse;
import com.example.ivsmirnov.keyregistrator.others.Values;

/**
 * Created by ivsmirnov on 02.02.2016.
 */
public class NFC_Reader{

    public static class openReader extends AsyncTask<UsbDevice, Void , Void>{

        private Reader mReader;

        public openReader(Reader reader){
            this.mReader = reader;
        }

        @Override
        protected Void doInBackground(UsbDevice... params) {
            try {
                mReader.open(params[0]);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mReader!=null){
                Log.d("ReaderName: ", mReader.getReaderName());
            }
        }
    }

    public String getTag(Reader reader, ReaderResponse response){
        new setPower(reader).execute(getPowerParams());
        new setProtocol(reader).execute(getProtocolParams());
        new transmit(reader, response).execute(getTransmitParams());
        return null;
    }

    private class setPower extends AsyncTask<PowerParams,Void,Void>{
        private Reader mReader;
        private setPower(Reader reader){
            this.mReader = reader;
        }

        @Override
        protected Void doInBackground(PowerParams... params) {
            try {
                mReader.power(params[0].slotNum, params[0].action);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class setProtocol extends AsyncTask<ProtocolParams,Void,Void>{
        private Reader mReader;
        private setProtocol(Reader reader){
            this.mReader = reader;
        }
        @Override
        protected Void doInBackground(ProtocolParams... params) {
            try {
                mReader.setProtocol(params[0].slotNum, params[0].preferredProtocols);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class transmit extends AsyncTask<TransmitParams,Void,TransmitResult>{
        public ReaderResponse mListener = null;
        private Reader mReader;
        private transmit(Reader reader, ReaderResponse readerResponse){
            this.mReader = reader;
            this.mListener = readerResponse;
        }

        @Override
        protected TransmitResult doInBackground(TransmitParams... params) {
            TransmitResult result = new TransmitResult();
            byte[] command = params[0].command;
            byte[] response = new byte[100];
            try {
                result.responseLength = mReader.transmit(params[0].slotNum,
                            command,
                            command.length,
                            response,
                            response.length);
                result.response = response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(TransmitResult transmitResult) {
            if (transmitResult.responseLength!=0){
                String tag = getStringFromByte(transmitResult.response, transmitResult.responseLength-2) + "00 00";
                mListener.onGetResult("taaaagololopspsps");
            }
        }
    }

    private PowerParams getPowerParams(){
        PowerParams params = new PowerParams();
        params.slotNum = 0;
        params.action = 2;
        return params;
    }

    private ProtocolParams getProtocolParams(){
        int prefferedProtocol = Reader.PROTOCOL_UNDEFINED;
        String prefferedProtocolString = "";
        prefferedProtocol |= Reader.PROTOCOL_T0;
        prefferedProtocolString = "T0";
        prefferedProtocol |= Reader.PROTOCOL_T1;
        prefferedProtocolString +="/T1";
        if (prefferedProtocolString==""){
            prefferedProtocolString="None";
        }
        ProtocolParams params = new ProtocolParams();
        params.preferredProtocols = prefferedProtocol;
        params.slotNum = 0;
        return params;
    }

    private TransmitParams getTransmitParams(){
        TransmitParams params = new TransmitParams();
        params.slotNum = 0;
        params.controlCode = -1;
        params.command = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04};
        return params;
    }

    private static class PowerParams {
        public int slotNum;
        public int action;
    }
    private static class ProtocolParams {
        public int slotNum;
        public int preferredProtocols;
    }
    private static class TransmitParams {
        public int slotNum;
        public int controlCode;
        public byte [] command;
    }
    private static class TransmitResult {
        public byte[] response;
        public int responseLength;
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
