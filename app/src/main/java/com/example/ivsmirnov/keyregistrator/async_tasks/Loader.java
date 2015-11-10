package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.FinishLoad;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.FileManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Loader extends AsyncTask <String,Integer,Void> {

    private Context context;
    private String absPath;
    private ProgressDialog dialog;
    private SharedPreferences preferences;
    private FileManager fileManagerActivity;
    private FinishLoad listener;
    private int LOAD_TYPE;


    public Loader (Context c,FileManager activity,String abs,int load,FinishLoad l){
        context = c;
        absPath = abs;
        fileManagerActivity = activity;
        LOAD_TYPE = load;
        listener = l;
        dialog = new ProgressDialog(fileManagerActivity);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Загрузка...");
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        //dialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (dialog.isShowing()){
            dialog.cancel();
        }
        listener.onFinish();


        Toast.makeText(context,"Готово!",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(String... params) {

        DataBaseJournal dbJournal = new DataBaseJournal(context);
        DataBaseFavorite dbFavorite = new DataBaseFavorite(context);
        ArrayList<String> items = null;
        int count = 0;

        if (LOAD_TYPE == Values.LOAD_JOURNAL){
            dbJournal.clearJournalDB();
            try {
                items = FileManager.readFile(absPath);
                dialog.setMax(preferences.getInt(Values.LINES_COUNT_IN_FILE,0));
            } catch (IOException e){
                e.printStackTrace();
            }
            if (items != null) {
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
                    dbJournal.writeInDBJournal(aud, name, time, timePut, true);
                    //publishProgress(count++);

                }
            }

        }else if (LOAD_TYPE == Values.LOAD_TEACHERS){
            dbFavorite.clearTeachersDB();
            try {
                FileManager.readLine(context, absPath, 2);
                //items = FileManager.readFile(absPath);
                //dialog.setMax(preferences.getInt(Values.LINES_COUNT_IN_FILE,0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (LOAD_TYPE ==66){

            try {
                FileManager.readLine(context, absPath, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            /*try {
                items = FileManager.readFile(absPath);
                dialog.setMax(preferences.getInt(Values.LINES_COUNT_IN_FILE,0));
            } catch (IOException e){
                e.printStackTrace();
            }
            db.clearBaseSQL();

            String delims = ";";
            if (items != null) {
                for (String s : items){
                    String [] split = s.split(delims);
                    String kaf = split[0];
                    String name = split[1];
                    String surname = split[2];
                    String lastname = split[3];
                    String photo = split[4];
                    db.writeInDBSQL(kaf,name,surname,lastname,photo);
                   publishProgress(count++);
                }
            }*/
        }else if(LOAD_TYPE==67){
            try {
                FileManager.readLine(context,absPath,3);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show();
        }
        dbJournal.closeDB();
        dbFavorite.closeDB();

        return null;
    }


    private static long parseDate(String text)
            throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss",
                new Locale("ru"));
        return dateFormat.parse(text).getTime();
    }


}
