package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.custom_views.JournalItem;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ivsmirnov on 13.11.2015.
 */
public class Loader_intent extends AsyncTask<Void,Integer,Void> {

    private Context mContext;
    private String mPath;
    private ProgressDialog progressDialog;
    private UpdateInterface mListener;
    private int mLoadType;

    public Loader_intent(Context context, String path, UpdateInterface listener, int loadType){
        this.mContext = context;
        this.mPath = path;
        this.mListener = listener;
        this.mLoadType = loadType;
        progressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Загрузка...");
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        File file;
        int count = 0;
        BufferedReader fin = null;
        int i = 0;
        String line;
        ArrayList<String> lines = new ArrayList<>();
        if (mPath!=null){
             file = new File(mPath);
            try {
                fin = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            count = getStringCount(file);

            progressDialog.setMax(count);

             lines = new ArrayList<>(count);
        }

        switch (mLoadType){
            case Values.LOAD_TEACHERS:
                DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);
                dbFavorite.clearTeachersDB();
                try {
                    while ((line = fin.readLine())!=null){
                        if (i<count){
                            if (!lines.contains(line)){
                                String [] split = line.split(";");
                                if (split.length==7){
                                   // dbFavorite.writeCardInBase(split[0], split[1], split[2], split[3], split[4], split[5], split[6]);
                                    publishProgress(i);
                                    i++;
                                }
                            }
                        }
                    }
                    dbFavorite.closeDB();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Values.LOAD_JOURNAL:
                DataBaseJournal dbJournal = new DataBaseJournal(mContext);
                dbJournal.clearJournalDB();
                try {
                    if (fin != null) {
                        while ((line = fin.readLine())!=null){
                            if (i<count){
                                try {
                                    String [] split = line.split(";");
                                    String aud = split[0];
                                    Long timeIn = Long.parseLong(split[1]);
                                    Long timeOut = Long.parseLong(split[2]);
                                    int accessType = Integer.parseInt(split[3]);
                                    String personLastname = split[4];
                                    String personFirstname = split[5];
                                    String personMidname = split[6];
                                    String personPhoto = split[7];
                                    dbJournal.writeInDBJournal(new JournalItem(aud,timeIn,timeOut,accessType,personLastname,personFirstname,personMidname,personPhoto));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                publishProgress(i);
                                i++;
                            }
                        }
                    }
                    dbJournal.closeDB();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Values.LOAD_ROOMS:
                DataBaseRooms dbRooms = new DataBaseRooms(mContext);
                dbRooms.clearRoomsDB();
                try {
                    while ((line = fin.readLine())!=null){
                        if (i<count){
                            if (!lines.contains(line)){
                                String [] split = line.split(";");
                                if(split.length==6){
                                    dbRooms.writeInRoomsDB(new RoomItem(
                                            split[0],
                                            Integer.parseInt(split[1]),
                                            Integer.parseInt(split[2]),
                                            0,
                                            split[3],
                                            split[4],
                                            split[5]));
                                    publishProgress(i);
                                    i++;
                                }
                            }
                        }
                    }
                    dbRooms.closeDB();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog.isShowing()){
            progressDialog.cancel();
        }
        mListener.updateInformation();
        Toast.makeText(mContext, "Готово!", Toast.LENGTH_SHORT).show();
    }

    public static int getStringCount(File file)
    {
        int i=0;
        BufferedReader bufferedReader = null;
        try{
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while(bufferedReader.readLine()!=null)
                i++;
            bufferedReader.close();
        }catch(Exception e){}
        return i;
    }

    private static long parseDate(String text)
            throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss",
                new Locale("ru"));
        return dateFormat.parse(text).getTime();
    }
}
