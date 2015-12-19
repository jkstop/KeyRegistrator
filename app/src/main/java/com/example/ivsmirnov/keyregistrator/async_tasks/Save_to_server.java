package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 10.12.2015.
 */
public class Save_to_server extends AsyncTask <Void,Void,Void> {

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private ProgressDialog mProgressDialog;

    public Save_to_server (Context context){
        this.mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Запись на сервер...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        ArrayList<SparseArray> mItems = dbJournal.readJournalFromDB();
        dbJournal.closeDB();

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
            PreparedStatement trunacteTable = conn.prepareStatement("TRUNCATE TABLE JOURNAL");
            trunacteTable.execute();
            for (int i=0;i<mItems.size();i++){
                SparseArray row = mItems.get(i);
                PreparedStatement preparedStatement  = conn.prepareStatement("INSERT INTO JOURNAL VALUES ('"+row.get(0)+"','"+row.get(1)+"','"+row.get(2)+"','"+row.get(3)+"')");
                preparedStatement.execute();
            }


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
    }
}
