package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.fragments.Search_Fragment;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 07.12.2015.
 */
public class Find_User_in_SQL_Server extends AsyncTask<ResultSet,Void,ArrayList<SparseArray>> {

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
    protected ArrayList<SparseArray> doInBackground(ResultSet... params) {
        ArrayList<SparseArray> mItems = new ArrayList<SparseArray>();
        try {
            while (params[0].next()){
                SparseArray mSparce = new SparseArray();
                mSparce.put(0,params[0].getString("LASTNAME"));
                mSparce.put(1,params[0].getString("FIRSTNAME"));
                mSparce.put(2,params[0].getString("MIDNAME"));
                mSparce.put(3,params[0].getString("NAME_DIVISION"));
                mSparce.put(4,params[0].getString("SEX"));
                mSparce.put(5,params[0].getString("PHOTO"));
                mSparce.put(6,params[0].getString("RADIO_LABEL"));
                mItems.add(mSparce);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mItems;
    }

    @Override
    protected void onPostExecute(ArrayList<SparseArray> sparseArrays) {
        super.onPostExecute(sparseArrays);
        mListener.changeProgressBar(View.INVISIBLE);
        mListener.updateGrid(sparseArrays);
    }
}
