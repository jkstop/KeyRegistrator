package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.os.AsyncTask;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.interfaces.TagSearcherInterface;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Поиск радиометки
 */
public class TagSearcher extends AsyncTask<Connection,Void,ArrayList<String>> {

    private TagSearcherInterface mTagSearcherInterface;
    private CharSequence mSearchString;

    public TagSearcher(CharSequence searchString, TagSearcherInterface tagSearcherInterface){
        this.mSearchString = searchString;
        mTagSearcherInterface = tagSearcherInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("tag searcher ************************************");
        mTagSearcherInterface.changeProgressBar(View.VISIBLE);
    }

    @Override
    protected ArrayList<String> doInBackground(Connection... params) {
        ArrayList<String> mItems = new ArrayList<>();
        try {
            Statement statement = params[0].createStatement();
            ResultSet resultSet = statement.executeQuery("select [RADIO_LABEL] from STAFF_NEW where [LASTNAME] like '"+ mSearchString +"%'");
            while (resultSet.next()){

                mItems.add(resultSet.getString("RADIO_LABEL"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mItems;
    }

    @Override
    protected void onPostExecute(ArrayList<String> personTags) {
        System.out.println("tag searcher ---------------------------------");
        mTagSearcherInterface.changeProgressBar(View.INVISIBLE);
        mTagSearcherInterface.updateGrid(personTags);
    }

}
