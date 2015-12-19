package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Search_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Loader_Image extends AsyncTask <Void, Void, Void> {

    private Context context;
    private String surname;
    private String name;
    private String lastname;
    private String kaf;
    private String gender;
    private int pos;
    private String tag;

    private ProgressDialog dialog;
    private Search_Fragment activity;

    private SharedPreferences mPreferences;

    public Loader_Image (Context c,String [] items,Search_Fragment a){
        this.context = c;
        this.surname = items[0];
        this.name = items[1];
        this.lastname = items[2];
        this.kaf = items[3];
        this.gender = items[4];
        this.tag = items[5];
        this.activity = a;

        dialog = new ProgressDialog(activity.getActivity());

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

        String ip = mPreferences.getString(Values.SQL_SERVER,"");
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "KeyRegistratorBase";
        String user = mPreferences.getString(Values.SQL_USER,"");
        String password = mPreferences.getString(Values.SQL_PASSWORD,"");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection conn = null;
        String ConnURL = null;

        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "database=" + db +";user=" + user + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from STAFF where [NAME_DIVISION] ='"+kaf+"' and [LASTNAME] ='"+surname+"' and [FIRSTNAME] ='"+name+"' and [MIDNAME] ='"+lastname+"'");
            String photo = "null";
            while (resultSet.next()){
                photo = resultSet.getString("PHOTO");
            }
            DataBaseFavorite dbFavorite = new DataBaseFavorite(context);
            dbFavorite.writeCardInBase(surname,name,lastname,kaf,tag,gender,photo);
            dbFavorite.closeDB();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (dialog.isShowing()){
            dialog.cancel();
        }
        Snackbar snackbar = Snackbar.make(activity.getView(), R.string.added,Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}
