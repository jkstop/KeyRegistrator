package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 07.12.2015.
 */
public class Find_User_in_SQL_Server extends AsyncTask<Connection,Void,ArrayList<PersonItem>> {

    private Find_User_in_SQL_Server_Interface mListener;
    private Context mContext;
    private CharSequence mSearchString;

    public Find_User_in_SQL_Server (Context context, CharSequence searchString, Find_User_in_SQL_Server_Interface listener){
        this.mContext = context;
        this.mSearchString = searchString;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.changeProgressBar(View.VISIBLE);
    }

    @Override
    protected ArrayList<PersonItem> doInBackground(Connection... params) {
        ArrayList<PersonItem> mItems = new ArrayList<>();
        try {
            Statement statement = params[0].createStatement();
            ResultSet resultSet = statement.executeQuery("select * from STAFF_NEW where [LASTNAME] like '"+ mSearchString +"%'");
            while (resultSet.next()){


                mItems.add(new PersonItem()
                        .setLastname(resultSet.getString("LASTNAME"))
                        .setFirstname(resultSet.getString("FIRSTNAME"))
                        .setMidname(resultSet.getString("MIDNAME"))
                        .setDivision(resultSet.getString("NAME_DIVISION"))
                        .setSex(resultSet.getString("SEX"))
                        .setRadioLabel(resultSet.getString("RADIO_LABEL")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mItems;
    }

    @Override
    protected void onPostExecute(ArrayList<PersonItem> personItems) {
        super.onPostExecute(personItems);
        mListener.changeProgressBar(View.INVISIBLE);
        mListener.updateGrid(personItems);
    }

}
