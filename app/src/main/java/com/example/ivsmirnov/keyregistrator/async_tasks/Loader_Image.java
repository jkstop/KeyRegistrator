package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.fragments.Dialog_Fragment;
import com.example.ivsmirnov.keyregistrator.fragments.Search_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.FinishLoad;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateTeachers;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Loader_Image extends AsyncTask <Void, Void, Void> {

    private Context context;
    private PersonItem mPersonItem;

    private ProgressDialog dialog;
    private Search_Fragment activity;

    private SharedPreferences mPreferences;

    public Loader_Image (Context c, PersonItem personItem, Search_Fragment a){
        this.context = c;
        this.mPersonItem = personItem;
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
            ResultSet resultSet = statement.executeQuery("select * from STAFF where " +
                    "[NAME_DIVISION] ='"+mPersonItem.Division+"' " +
                    "and [LASTNAME] ='" +mPersonItem.Lastname+
                    "' and [FIRSTNAME] ='" +mPersonItem.Firstname+
                    "' and [MIDNAME] ='" +mPersonItem.Midname+"'");
            String photo = null;
            while (resultSet.next()){
                photo = resultSet.getString("PHOTO");
            }
            if (photo==null){
                Bitmap bitmap;
                if(mPersonItem.Sex.equals("Ж")){
                    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.person_female_colored);
                }else{
                    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.person_male_colored);
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                photo = Base64.encodeToString(byteArray,Base64.NO_WRAP);
            }
            DataBaseFavorite dbFavorite = new DataBaseFavorite(context);
            dbFavorite.writeInDBTeachers(mPersonItem.Lastname,
                    mPersonItem.Firstname,
                    mPersonItem.Midname,
                    mPersonItem.Division,
                    mPersonItem.RadioLabel,
                    mPersonItem.Sex,
                    photo);
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

        if (activity.getView()!=null){
            Snackbar.make(activity.getView(),R.string.snack_user_added,Snackbar.LENGTH_LONG)
                    .setAction(R.string.snack_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DataBaseFavorite dataBaseFavorite = new DataBaseFavorite(context);
                            dataBaseFavorite.deleteFromTeachersDB(mPersonItem);
                            dataBaseFavorite.closeDB();
                            Snackbar.make(activity.getView(),R.string.snack_cancelled,Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .show();
        }
    }

}
