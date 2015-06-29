package com.example.ivsmirnov.keyregistrator;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.app.FragmentManager;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by ivsmirnov on 26.06.2015.
 */
public class Loader extends AsyncTask <String,Void,Void> {

    private Context context;
    private String absPath;
    private FragmentManager fragmentManager;

    public Loader (Context c,String abs,FragmentManager fm){
        context = c;
        absPath = abs;
        fragmentManager = fm;
    }

//chg
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(String... params) {



        DataBases db = new DataBases(context);
        db.clearJournalDB();
        String [] items = null;
        try {
            items = FileManager.readFile(context,absPath);
        } catch (IOException e){
            e.printStackTrace();
        }
        for (String s : items){
            String [] split = s.split("\\s+");

            String aud = "";
            String name = "";
            Long time = (long)1;
            Long timePut = (long)1;

            if(split.length>5){
                aud = split[0];
                int nameIndexLast = split.length-3;
                for (int i=1;i<=nameIndexLast;i++){
                    name += split[i]+" ";
                }
                try {
                    time = parseDate(split[split.length-2]);
                    timePut = parseDate(split[split.length-1]);
                } catch (ParseException e) {
                    time = (long)1;
                    timePut = (long)1;
                }
            }else if(split.length<5){
                if (split[0].length()!=3){
                    aud = "_";
                    for (int i=0;i<=split.length-1;i++){
                        name += split[i]+" ";
                    }
                }else{
                    aud = split[0];
                    for (int i = 1;i<=split.length-3;i++){
                        name += split[i]+" ";
                    }
                    try {
                        time = parseDate(split[split.length-2]);
                        timePut = parseDate(split[split.length-1]);
                    } catch (ParseException e) {
                        time = (long)1;
                        timePut = (long)1;
                    }
                }
            }else{
                aud = split[0];
                name = split[1]+ " "+ split[2];
                try {
                    time = parseDate(split[3]);
                    timePut = parseDate(split[4]);
                } catch (ParseException e) {
                    time = (long)1;
                    timePut = (long)1;
                }
            }
            db.writeInDBJournal(aud,name,time,timePut,true);

        }
        db.closeDBconnection();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("DONE!","");
        Dialog_Fragment dialog = new Dialog_Fragment(context,Values.DIALOG_LOADING);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        dialog.show(fragmentTransaction, "load");//не работает
    }

    private static long parseDate(String text)
            throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss",
                new Locale("ru"));
        return dateFormat.parse(text).getTime();
    }
}
