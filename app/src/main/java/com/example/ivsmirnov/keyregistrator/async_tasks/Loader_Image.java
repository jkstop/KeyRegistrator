package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.activities.Add_user;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;

/**
 * Created by IVSmirnov on 27.07.2015.
 */
public class Loader_Image extends AsyncTask <Void, Void, Void> {

    private Context context;
    private String surname;
    private String name;
    private String lastname;
    private String kaf;
    private String gender;

    private ProgressDialog dialog;
    private Add_user activity;

    private OnImageLoaded listener;

    public Loader_Image (Context c,String [] items,Add_user a,OnImageLoaded l){
        this.context = c;
        this.surname = items[0];
        this.name = items[1];
        this.lastname = items[2];
        this.kaf = items[3];
        this.gender = items[4];

        this.activity = a;
        this.listener = l;

        dialog = new ProgressDialog(activity);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Подождите, идет загрузка...");
        dialog.show();

    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBases db = new DataBases(context);
        db.writeCardInBase(surname, name, lastname, kaf, gender);
        db.closeDBconnection();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (dialog.isShowing()){
            dialog.cancel();
        }
        listener.doneLoad(true);
    }

    public interface OnImageLoaded{
        void doneLoad(boolean isDone);
    }

}
