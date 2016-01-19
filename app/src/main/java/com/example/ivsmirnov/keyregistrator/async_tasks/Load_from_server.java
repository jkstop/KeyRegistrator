package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.example.ivsmirnov.keyregistrator.custom_views.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ivsmirnov on 10.12.2015.
 */
public class Load_from_server extends AsyncTask<Void,Void,Void> {

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;

    public Load_from_server(Context context, UpdateInterface updateInterface){
        this.mContext = context;
        this.mListener = updateInterface;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Загрузка с сервера...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        String ip = mSharedPreferences.getString(Values.SQL_SERVER,"");
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "KeyRegistratorBase";
        String user = mSharedPreferences.getString(Values.SQL_USER,"");
        String password = mSharedPreferences.getString(Values.SQL_PASSWORD,"");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "database=" + db + ";user=" + user + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM Journal_recycler");
            DataBaseJournal dbjournal = new DataBaseJournal(mContext);
            dbjournal.clearJournalDB();
            while (result.next()){
                String aud = result.getString("AUDITROOM");
                Long time_in = Long.parseLong(result.getString("TIME_IN"));
                Long time_out = Long.parseLong(result.getString("TIME_OUT"));
                int access = result.getInt("ACCESS");
                String person_lastname = result.getString("PERSON_LASTNAME");
                String person_firstname = result.getString("PERSON_FIRSTNAME");
                String person_midname = result.getString("PERSON_MIDNAME");
                String person_photo = result.getString("PERSON_PHOTO");

                dbjournal.writeInDBJournal(new JournalItem(aud,time_in,time_out,access,person_lastname,person_firstname,person_midname,person_photo));
            }
            dbjournal.closeDB();
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
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
        mListener.updateInformation();
    }
}
