package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Close_day_task extends AsyncTask<Void,Void,Void> {

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public Close_day_task(Context context){
        this.mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBaseJournal dbJournal = new DataBaseJournal(mContext);
        DataBaseFavorite dbFavorite = new DataBaseFavorite(mContext);

        //dbJournal.backupJournalToFile();
        dbFavorite.backupFavoriteStaffToFile();

        dbFavorite.closeDB();

        //ArrayList<SparseArray> mItems = dbJournal.readJournalFromDB();
/*
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
        dbJournal.closeDB();*/
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(mContext,"Запись произошла успешно",Toast.LENGTH_SHORT).show();
    }
}
