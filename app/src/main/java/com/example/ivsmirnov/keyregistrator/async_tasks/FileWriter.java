package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Запись файлов
 */
public class FileWriter extends AsyncTask <Void,Integer,Void> {

    public static final int WRITE_JOURNAL = 100;
    public static final int WRITE_TEACHERS = 101;
    public static final int WRITE_ROOMS = 123;

    private int mType;
    private ProgressDialog mProgressDialog;
    private String mPathExternal;
    private boolean isShowDialog;


    private static final String JOURNAL = "/Journal.xls";
    private static final String TEACHERS = "/Teachers.csv";

    public FileWriter(Context context, int loadType, boolean isShowDialog){
        this.mType = loadType;
        this.isShowDialog = isShowDialog;
        mPathExternal = Environment.getExternalStorageDirectory().getPath();
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        System.out.println("file writer ***********************************");
        if (isShowDialog){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Запись...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        switch (mType){
            case WRITE_JOURNAL:

                JournalDB.backupJournalToXLS();
                JournalDB.backupJournalToCSV();

                String srFileJournal = mPathExternal + JOURNAL;
                String dtFileJournal = Settings.getJournalBackupLocation() + JOURNAL;
                copyFile(srFileJournal, dtFileJournal);
                break;
            case WRITE_TEACHERS:

                FavoriteDB.backupFavoriteStaffToFile();

                String srFileTeachers = mPathExternal + TEACHERS;
                String dtFileTeachers = Settings.getPersonsBackupLocation() + TEACHERS;
                copyFile(srFileTeachers, dtFileTeachers);
                break;
            case WRITE_ROOMS:
                RoomDB.backupRoomsToFile();
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        System.out.println("file writer ------------------------------------------");
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }

    public static void copyFile(String srFile, String dtFile){
        try{
            File f1 = new File(srFile);
            File f2 = new File(dtFile);
            InputStream in = new FileInputStream(f1);
            OutputStream out = new FileOutputStream(f2);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        catch(FileNotFoundException ex){
            System.out.println(ex.getMessage() + " in the specified directory.");
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}
