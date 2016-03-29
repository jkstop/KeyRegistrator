package com.example.ivsmirnov.keyregistrator.services;

import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.example.ivsmirnov.keyregistrator.interfaces.ReaderInterface;

/**
 * методы NFC считывателя
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
            if (mReader.getReaderName()!=null){
                Log.d("ReaderName: ", mReader.getReaderName());
            }
        }
    }

    public void getTag(Reader reader, ReaderInterface response){
        new setPower(reader).execute(getPowerParams());
        new setProtocol(reader).execute(getProtocolParams());
        new transmit(reader, response).execute(getTransmitParams());
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
        public ReaderInterface mListener = null;
        private Reader mReader;
        private transmit(Reader reader, ReaderInterface readerInterface){
            this.mReader = reader;
            this.mListener = readerInterface;
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

                mListener.onGetPersonTag(tag);


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
        //String prefferedProtocolString;
        prefferedProtocol |= Reader.PROTOCOL_T0;
        //prefferedProtocolString = "T0";
        prefferedProtocol |= Reader.PROTOCOL_T1;
        //prefferedProtocolString +="/T1";
        //if (prefferedProtocolString == null) prefferedProtocolString="None";

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
                if (!bufferString.equals("")) {
                    bufferString = "";
                }
            }
            bufferString += hexChar.toUpperCase() + " ";
        }
        return bufferString;
    }
}
