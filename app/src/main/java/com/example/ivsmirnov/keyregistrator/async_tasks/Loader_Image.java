package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;

public class Loader_Image extends AsyncTask <Void, Void, Void> {

    private Context context;
    private String surname;
    private String name;
    private String lastname;
    private String kaf;
    private String gender;
    private int pos;

    private ProgressDialog dialog;
    private Dialog_Fragment activity;

    private UpdateTeachers listener;

    public Loader_Image (Context c,String [] items,Dialog_Fragment a,UpdateTeachers l){
        this.context = c;
        this.surname = items[0];
        this.name = items[1];
        this.lastname = items[2];
        this.kaf = items[3];
        this.gender = items[4];
        this.pos = Integer.parseInt(items[5]);

        this.activity = a;
        this.listener = l;

        dialog = new ProgressDialog(activity.getActivity());
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
        DataBaseFavorite dbFavorite = new DataBaseFavorite(context);
        //String photo = db.findPhotoByPosition(pos);
        //dbFavorite.writeCardInBase(surname,name,lastname,kaf,"null",gender,photo);
        db.closeDBconnection();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (dialog.isShowing()){
            dialog.cancel();
        }
        listener.onFinishEditing();
    }



}
