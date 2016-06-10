package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.others.App;

import java.util.ArrayList;

/**
 * Запись файлов
 */
public class FileWriter extends AsyncTask <ArrayList<String>,Integer,Exception> {

    public static final int WRITE_JOURNAL = 100;
    public static final int WRITE_TEACHERS = 101;
    public static final int WRITE_ROOMS = 123;

    private int mType;
    private ProgressDialog mProgressDialog;
    private String mPathExternal;
    private boolean isShowDialog;


    //private static final String JOURNAL = "/Journal.xls";
    //private static final String TEACHERS = "/Teachers.csv";

    public FileWriter(Context context,  boolean isShowDialog){
        if (isShowDialog){
            mProgressDialog = new ProgressDialog(context);
        }
    }

    @Override
    protected void onPreExecute() {
        System.out.println("file writer ***********************************");
        if (mProgressDialog!=null){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Запись...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Exception doInBackground(ArrayList<String>... params) {
        String [] allWriteItems = App.getAppContext().getResources().getStringArray(R.array.shared_preferences_backup_items_entries);

        try {
            if (params[0].contains(allWriteItems[0])){
                //journal
                publishProgress(WRITE_JOURNAL);
                JournalDB.backupJournalToXLS();
                JournalDB.backupJournalToCSV();

                System.out.println("journal backuped");

                // String srFileJournal = mPathExternal + JOURNAL;
                // String dtFileJournal = SharedPrefs.getJournalBackupLocation() + JOURNAL;
                // copyFile(srFileJournal, dtFileJournal);
            }

            if (params[0].contains(allWriteItems[1])){
                //persons
                publishProgress(WRITE_TEACHERS);
                FavoriteDB.backupFavoriteStaffToFile();

                System.out.println("persons backuped");

                //String srFileTeachers = mPathExternal + TEACHERS;
                //String dtFileTeachers = SharedPrefs.getPersonsBackupLocation() + TEACHERS;
                //copyFile(srFileTeachers, dtFileTeachers);
            }

            if (params[0].contains(allWriteItems[2])){
                //rooms
                publishProgress(WRITE_ROOMS);
                RoomDB.backupRoomsToFile();

                System.out.println("rooms backuped");
            }
        } catch (Exception e){
            e.printStackTrace();
            return e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mProgressDialog!=null){
            switch (values[0]){
                case WRITE_JOURNAL:
                    mProgressDialog.setMessage("Запись журнала...");
                    break;
                case WRITE_TEACHERS:
                    mProgressDialog.setMessage("Запись списка пользователей...");
                    break;
                case WRITE_ROOMS:
                    mProgressDialog.setMessage("Запись списка помещений...");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onPostExecute(Exception e) {
        System.out.println("file writer ------------------------------------------");
        if (e == null){
            System.out.println("success");
        } else {
            System.out.println("error");
        }

        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }

    /*public static void copyFile(String srFile, String dtFile){
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
    }*/
}
