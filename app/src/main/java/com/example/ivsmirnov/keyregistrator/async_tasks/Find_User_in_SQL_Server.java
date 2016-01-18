package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.fragments.Search_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 07.12.2015.
 */
public class Find_User_in_SQL_Server extends AsyncTask<ResultSet,Void,ArrayList<PersonItem>> {

    private Find_User_in_SQL_Server_Interface mListener;

    public Find_User_in_SQL_Server (Find_User_in_SQL_Server_Interface listener){
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.changeProgressBar(View.VISIBLE);
    }

    @Override
    protected ArrayList<PersonItem> doInBackground(ResultSet... params) {
        ArrayList<PersonItem> mItems = new ArrayList<>();
        try {
            while (params[0].next()){
                mItems.add(new PersonItem(params[0].getString("LASTNAME"),
                        params[0].getString("FIRSTNAME"),
                        params[0].getString("MIDNAME"),
                        params[0].getString("NAME_DIVISION"),
                        params[0].getString("SEX"),
                        null,
                        params[0].getString("PHOTO"),
                        params[0].getString("RADIO_LABEL")));
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
