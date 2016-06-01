package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;

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
 * Загрузка из файла
 */
public class Loader_intent extends AsyncTask<Void,Integer,Boolean> {

    public static final int REQUEST_CODE_LOAD_FAVORITE_STAFF = 200;
    public static final int REQUEST_CODE_LOAD_JOURNAL = 201;
    public static final int REQUEST_CODE_LOAD_ROOMS = 202;

    private Context mContext;
    private String mPath;
    private ProgressDialog progressDialog;
    private UpdateInterface mListener;
    private int mLoadType;
    private boolean isFileVerify = false;

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
        System.out.println("loader intent **************************");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Загрузка...");
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File file;
        int count = 0;
        BufferedReader bufferedReader = null;
        int i = 0;
        String line;
        ArrayList<String> lines = new ArrayList<>();
        if (mPath!=null){
             file = new File(mPath);
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            count = getStringCount(file);

            progressDialog.setMax(count);

             lines = new ArrayList<>(count);
        }

        switch (mLoadType){
            case REQUEST_CODE_LOAD_FAVORITE_STAFF:

                FavoriteDB.clear();
                try {
                    String verify = bufferedReader.readLine();
                    if (verify.equals(FavoriteDB.PERSONS_VALIDATE)){
                        isFileVerify = true;
                        while ((line = bufferedReader.readLine())!=null){
                            if (i<count && !lines.contains(line)){
                                try {
                                    String [] split = line.split(";");
                                    for (int j=0; j<split.length; j++){
                                        if (split[j].equals("null")) split[j] = null;
                                    }

                                    FavoriteDB.addNewUser(new PersonItem()
                                            .setLastname(split[0])
                                            .setFirstname(split[1])
                                            .setMidname(split[2])
                                            .setDivision(split[3])
                                            .setSex(split[4])
                                            .setRadioLabel(split[5])
                                            .setPhoto(split[6]));

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
            case REQUEST_CODE_LOAD_JOURNAL:
                JournalDB.clearJournalDB();
                try {
                    String verify = bufferedReader.readLine();
                    if (verify.equals("pass")){
                        isFileVerify = true;
                            while ((line = bufferedReader.readLine())!=null){
                                if (i<count){
                                    String [] split = line.split(";");

                                    JournalDB.writeInDBJournal(new JournalItem()
                                            .setAccountID(split[0])
                                            .setAuditroom(split[1])
                                            .setTimeIn(Long.parseLong(split[2]))
                                            .setTimeOut(Long.parseLong(split[3]))
                                            .setAccessType(Integer.parseInt(split[4]))
                                            .setPersonInitials(FavoriteDB.getPersonInitials(FavoriteDB.FULL_INITIALS, split[5], split[6], split[7])));

                                    publishProgress(i);
                                    i++;
                                }
                            }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_CODE_LOAD_ROOMS:
                RoomDB.clearRoomsDB();
                try {
                    String verify = bufferedReader.readLine();
                    if (verify.equals(RoomDB.ROOMS_VALIDATE)){
                        isFileVerify = true;
                        while ((line = bufferedReader.readLine())!=null){
                            if (i<count){
                                String [] split = line.split(";");

                                RoomDB.writeInRoomsDB(new RoomItem()
                                        .setAuditroom(split[0])
                                        .setStatus(Integer.parseInt(split[1]))
                                        .setAccessType(Integer.parseInt(split[2]))
                                        .setLastVisiter(split[3])
                                        .setTag(split[4]));

                                publishProgress(i);
                                i++;
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

        return isFileVerify;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean isFileVerify) {
        super.onPostExecute(isFileVerify);
        if (isFileVerify){
            Toast.makeText(mContext, "Готово!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Не правильный файл!", Toast.LENGTH_SHORT).show();
        }

        System.out.println("loader intent -----------------------------");
        if (progressDialog.isShowing()){
            progressDialog.cancel();
        }
        mListener.updateInformation();

    }

    public static int getStringCount(File file)
    {
        int i=0;
        BufferedReader bufferedReader;
        try{
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while(bufferedReader.readLine()!=null)
                i++;
            bufferedReader.close();
        }catch(Exception e){
            e.printStackTrace();
        }
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
