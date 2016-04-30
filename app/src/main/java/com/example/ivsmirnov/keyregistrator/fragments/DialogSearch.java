package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.util.AsyncListUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.ImageSaver;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Диалог поиска новых пользователей
 */
public class DialogSearch extends DialogFragment implements RecycleItemClickListener{

    private Connection mConnection;
    private SearchTask searchTask;
    private RecyclerView mPersonsRecycler;
    private AdapterPersonsGrid mPersonsAdapter;
    private ArrayList<PersonItem> mPersonList;

    private Context mContext;

    private int queryLength = 0;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog!=null){
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        System.out.println("dialog cancel");
        cancelSearchTask();
        clearTempFiles();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.view_dialog_search, container, false);
        mContext = getContext();
        mPersonList = new ArrayList<>();

        Toolbar toolbar = (Toolbar)dialogView.findViewById(R.id.dialog_search_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        mConnection = SQL_Connection.SQLconnect;

        mPersonsAdapter = new AdapterPersonsGrid(mContext, mPersonList, AdapterPersonsGrid.SHOW_ALL_PERSONS, this);
        mPersonsRecycler = (RecyclerView)dialogView.findViewById(R.id.dialog_search_recycler);
        mPersonsRecycler.setLayoutManager(new GridLayoutManager(mContext, 3));
        mPersonsRecycler.setAdapter(mPersonsAdapter);

        SearchView searchView = (SearchView)dialogView.findViewById(R.id.dialog_search_input);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("SUBMIT QUERY: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println("CHANGE QUERY: " + newText);
                System.out.println("SEARCH TASK " + searchTask);

                queryLength = newText.length();
                if (queryLength>=3 && queryLength<=6){
                    cancelSearchTask();
                    startSearchTask(newText);
                }

                return false;
            }
        });

        return dialogView;
    }

    private void clearTempFiles(){
        System.out.println("clear temp");
        File filesDir = ImageSaver.getCustomPath();
        if (filesDir.isDirectory()){
            File[]files = filesDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    private void cancelSearchTask(){
        if (searchTask!=null){
            searchTask.cancel(true);
        }
    }

    private void startSearchTask(String searchingText){
        searchTask = new SearchTask();
        searchTask.execute(searchingText);
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        System.out.println("clicked " + mPersonList.get(position).getLastname());
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {

    }

    private class SearchTask extends AsyncTask<String,PersonItem,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mPersonList!=null) mPersonList.clear();
            System.out.println("start task");
        }

        @Override
        protected Void doInBackground(String... params) {
            if (mConnection!=null){
                try {
                    ResultSet resultSet = mConnection.prepareStatement("SELECT "
                            + SQL_Connection.COLUMN_ALL_STAFF_DIVISION + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_LASTNAME + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_FIRSTNAME + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_MIDNAME + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_SEX + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_TAG
                            + " FROM " + SQL_Connection.ALL_STAFF_TABLE
                            + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_LASTNAME
                            + " LIKE '" + params[0] + "%'").executeQuery();
                    while (resultSet.next()){
                        if (isCancelled()) break;
                        PersonItem personItem = new PersonItem();
                        if (resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG)!=null){
                            ResultSet resultPhoto = mConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                                    .executeQuery("SELECT " + SQL_Connection.COLUMN_ALL_STAFF_PHOTO
                            + " FROM " + SQL_Connection.ALL_STAFF_TABLE
                            + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_TAG
                            + " = '" + resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG) + "'");
                            resultPhoto.first();
                            if (resultPhoto.getRow()!=0){
                                //personItem.setPhoto(resultPhoto.getString(SQL_Connection.COLUMN_ALL_STAFF_PHOTO));
                                String photo = resultPhoto.getString(SQL_Connection.COLUMN_ALL_STAFF_PHOTO);
                                String photoPath = new ImageSaver(mContext)
                                        .setFileName(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG))
                                        .save(photo, ImageSaver.TEMP);
                                personItem.setPhotoPath(photoPath);

                            }
                        }
                        publishProgress(personItem.setLastname(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_LASTNAME))
                                .setFirstname(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_FIRSTNAME))
                                .setMidname(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_MIDNAME))
                                .setDivision(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_DIVISION))
                                .setRadioLabel(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(PersonItem... values) {
            super.onProgressUpdate(values);
            mPersonList.add(values[0]);
            mPersonsAdapter.notifyDataSetChanged();
            System.out.println("PERSON FOUND: " + values[0].getPhotoPath());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            System.out.println("end task");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            System.out.println("cancel task");
        }
    }
}
