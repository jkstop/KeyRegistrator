package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.fragments.Journal;
import com.example.ivsmirnov.keyregistrator.fragments.Users;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Загрузка из файла
 */
public class FileLoader extends AsyncTask<Void,Integer,Boolean> {

    private Context mContext;
    private String mPath;
    private ProgressDialog progressDialog;
    private boolean isFileVerify = false;

    public FileLoader(Context context, String path){
        this.mContext = context;
        this.mPath = path;

        progressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Восстановление резервной копии...");
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File file;
        int count;
        BufferedReader bufferedReader;
        int i = 0;
        String line;

        try {
            file = new File(mPath);
            bufferedReader = new BufferedReader(new FileReader(file));
            count = getStringCount(file);
            progressDialog.setMax(count);

            String verify = bufferedReader.readLine();
            switch (verify) {
                case FavoriteDB.PERSONS_VALIDATE:
                    System.out.println("*************persons file***************");
                    isFileVerify = true;
                    FavoriteDB.clear();
                    while ((line = bufferedReader.readLine()) != null) {
                        if (i < count) {
                            try {
                                String[] split = line.split(";");
                                for (int j = 0; j < split.length; j++) {
                                    if (split[j].equals("null")) split[j] = null;
                                }

                                FavoriteDB.addNewUser(new PersonItem()
                                        .setLastname(split[0])
                                        .setFirstname(split[1])
                                        .setMidname(split[2])
                                        .setDivision(split[3])
                                        .setSex(split[4])
                                        .setRadioLabel(split[5])
                                        .setAccessType(Integer.parseInt(split[6]))
                                        .setPhoto(split[7]),
                                        SharedPrefs.getWriteServerStatus());

                                publishProgress(i);
                                i++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Users.contentNeedsForUpdate = true;
                    break;
                case JournalDB.JOURNAL_VALIDATE:
                    System.out.println("*************journal file**************");
                    isFileVerify = true;
                    JournalDB.clear();
                    while ((line = bufferedReader.readLine()) != null) {
                        if (i < count) {
                            String[] split = line.split(";");

                            JournalDB.writeInDBJournal(new JournalItem()
                                    .setAccountID(split[0])
                                    .setAuditroom(split[1])
                                    .setTimeIn(Long.parseLong(split[2]))
                                    .setTimeOut(Long.parseLong(split[3]))
                                    .setAccessType(Integer.parseInt(split[4]))
                                    .setPersonInitials(split[5])
                                    .setPersonTag(split[6]));

                            publishProgress(i);
                            i++;
                        }
                    }
                    Journal.contentNeedsForUpdate = true;
                    break;
                case RoomDB.ROOMS_VALIDATE:
                    System.out.println("************rooms file**********");
                    isFileVerify = true;
                    RoomDB.clear();
                    while ((line = bufferedReader.readLine()) != null) {
                        if (i < count) {
                            String[] split = line.split(";");

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
                    break;
                default:
                    break;
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
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

        if (progressDialog.isShowing()){
            progressDialog.cancel();
        }


    }

    private static int getStringCount(File file)
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
}
