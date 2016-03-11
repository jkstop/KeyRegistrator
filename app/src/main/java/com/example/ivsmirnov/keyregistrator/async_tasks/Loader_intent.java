package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Settings;
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
    private Settings mSettings;
    private ProgressDialog progressDialog;
    private UpdateInterface mListener;
    private int mLoadType;

    public Loader_intent(Context context, String path, UpdateInterface listener, int loadType){
        this.mContext = context;
        this.mPath = path;
        this.mListener = listener;
        this.mLoadType = loadType;
        progressDialog = new ProgressDialog(mContext);
        mSettings = new Settings(mContext);
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
            case Values.REQUEST_CODE_LOAD_FAVORITE_STAFF:

                DataBaseFavorite.clearTeachersDB();
                try {
                    while ((line = fin.readLine())!=null){
                        if (i<count){
                            if (!lines.contains(line)){
                                try {
                                    String [] split = line.split(";");

                                    DataBaseFavorite.writeInDBTeachers(mContext, new PersonItem()
                                            .setLastname(split[0])
                                            .setFirstname(split[1])
                                            .setMidname(split[2])
                                            .setDivision(split[3])
                                            .setSex(split[4])
                                            .setPhotoPreview(split[5])
                                            .setPhotoOriginal(split[6])
                                            .setRadioLabel(split[7]));
                                    publishProgress(i);
                                    i++;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Values.REQUEST_CODE_LOAD_JOURNAL:
                DataBaseJournal mDataBaseJournal;
                if (Launcher.mDataBaseJournal!=null){
                    mDataBaseJournal = Launcher.mDataBaseJournal;
                } else {
                    mDataBaseJournal = new DataBaseJournal(mContext);
                }
                mDataBaseJournal.clearJournalDB();
                try {
                    if (fin != null) {
                        while ((line = fin.readLine())!=null){
                            if (i<count){
                                try {
                                    String [] split = line.split(";");

                                    mDataBaseJournal.writeInDBJournal(new JournalItem()
                                    .setAccountID(split[0])
                                    .setAuditroom(split[1])
                                    .setTimeIn(Long.parseLong(split[2]))
                                    .setTimeOut(Long.parseLong(split[3]))
                                    .setAccessType(Integer.parseInt(split[4]))
                                    .setPersonLastname(split[5])
                                    .setPersonFirstname(split[6])
                                    .setPersonMidname(split[7])
                                    .setPersonPhoto(split[8]));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                publishProgress(i);
                                i++;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Values.REQUEST_CODE_LOAD_ROOMS:
                DataBaseRooms mDataBaseRooms;
                if (Launcher.mDataBaseRooms!=null){
                    mDataBaseRooms = Launcher.mDataBaseRooms;
                } else {
                    mDataBaseRooms = new DataBaseRooms(mContext);
                }
                mDataBaseRooms.clearRoomsDB();
                try {
                    while ((line = fin.readLine())!=null){
                        if (i<count){
                            if (!lines.contains(line)){
                                String [] split = line.split(";");
                                if(split.length==6){
                                    mDataBaseRooms.writeInRoomsDB(new RoomItem().setAuditroom(split[0])
                                            .setStatus(Integer.parseInt(split[1]))
                                            .setAccessType(Integer.parseInt(split[2]))
                                            .setLastVisiter(split[3])
                                            .setTag(split[4])
                                            .setPhoto(split[5]));
                                    publishProgress(i);
                                    i++;
                                }
                            }
                        }
                    }
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
