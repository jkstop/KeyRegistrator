package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.BaseWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.ImageSaver;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerReader;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.BaseWriterInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.items.BaseWriterParams;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.Settings;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Диалог поиска новых пользователей
 */
public class DialogSearch extends DialogFragment implements
        RecycleItemClickListener,
        SQL_Connection.Callback,
        ServerReader.Callback{

    private static final String BUNDLE_ROOM = "bundle_room";
    private static final int READ_USER = 100;
    private static final int GET_USER = 101;


   // private Connection mConnection;
    private SearchTask mSearchTask;
    private ProgressBar mProgressBar;
    private RecyclerView mPersonsRecycler;
    private AdapterPersonsGrid mPersonsAdapter;
    private ArrayList<PersonItem> mPersonList;
    private String mSelectedRoom;

    private String textSearch;
    private String selectedPersonTag;

    private Context mContext;

    private SQL_Connection.Callback mSQLCallback;
    private ServerReader.Callback mServerReaderCallback;

    private int queryLength = 0;

    public static DialogSearch newInstance (String selectedRoom){
        DialogSearch dialogSearch = new DialogSearch();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_ROOM, selectedRoom);
        dialogSearch.setArguments(bundle);
        return dialogSearch;
    }

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
        cancelSearchTask();
        clearTempFiles();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.view_dialog_search, container, false);
        mContext = getContext();
        mPersonList = new ArrayList<>();
        mSQLCallback = this;
        mServerReaderCallback = this;
        Bundle extras = getArguments();
        if (extras!=null){
            mSelectedRoom = extras.getString(BUNDLE_ROOM);
        }

        mProgressBar = (ProgressBar)dialogView.findViewById(R.id.dialog_search_progress);

        Toolbar toolbar = (Toolbar)dialogView.findViewById(R.id.dialog_search_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        //mConnection = SQL_Connection.getConnection(null, null);

        mPersonsAdapter = new AdapterPersonsGrid(mContext, mPersonList, AdapterPersonsGrid.SHOW_ALL_PERSONS, this);
        mPersonsRecycler = (RecyclerView)dialogView.findViewById(R.id.dialog_search_recycler);
        mPersonsRecycler.setLayoutManager(new GridLayoutManager(mContext, 3));
        mPersonsRecycler.setAdapter(mPersonsAdapter);

        final SearchView searchView = (SearchView)dialogView.findViewById(R.id.dialog_search_input);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryLength = newText.length();
                if (queryLength>=3 && queryLength<=6){

                    textSearch = newText;
                    SQL_Connection.getConnection(null, READ_USER, mSQLCallback);

                }

                return false;
            }
        });

        ImageButton addUserButton = (ImageButton)dialogView.findViewById(R.id.dialog_search_add_button);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = searchView.getQuery().toString();
                if (source.length() != 0){
                    String[] split = source.split("\\s+");
                    String lastname = null;
                    String firstname = null;
                    String midname = null;
                    String photo = FavoriteDB.getBase64DefaultPhotoFromResources();
                    try {
                        lastname = split[0].substring(0,1).toUpperCase() + split[0].substring(1, split[0].length());
                        firstname = split[1];
                        midname = split[2];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    addNewUser(new PersonItem().setLastname(lastname)
                            .setFirstname(firstname)
                            .setMidname(midname)
                            .setPhoto(photo)
                            .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                            .setRadioLabel(String.valueOf(new Random().nextLong() % (100000 - 1)) + 1));
                }
            }
        });

        return dialogView;
    }

    private void clearTempFiles(){
        File filesDir = ImageSaver.getCustomPath();
        if (filesDir.isDirectory()){
            File[]files = filesDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    private void cancelSearchTask(){
        if (mSearchTask !=null){
            mSearchTask.cancel(true);
        }
    }

    private void startSearchTask(Connection connection){
        mSearchTask = new SearchTask();
        mSearchTask.execute(connection);
    }

    @Override
    public void onItemClick(View v, int position, int viewID) {
        cancelSearchTask();
        selectedPersonTag = mPersonList.get(position).getRadioLabel();
        SQL_Connection.getConnection(null, GET_USER, mSQLCallback);
       // new TransportPersonTask().execute(mPersonList.get(position).getRadioLabel());
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {

    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        switch (callingTask){
            case READ_USER:
                cancelSearchTask();
                startSearchTask(connection);
                break;
            case GET_USER:
                new ServerReader(ServerReader.READ_PERSON_ITEM, selectedPersonTag, mServerReaderCallback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,connection);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerConnectException(Exception e) {

    }

    @Override
    public void onSuccessServerRead(Object result) {
        if (result!=null){
            PersonItem personItem = (PersonItem)result;

            addNewUser(personItem);

            if (mSelectedRoom!=null){
                new BaseWriter(mContext, (BaseWriterInterface)getActivity()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new BaseWriterParams()
                        .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                        .setAuditroom(mSelectedRoom)
                        .setPersonTag(personItem.getRadioLabel()));
            }
        }
    }

    @Override
    public void onErrorServerRead(Exception e) {

    }

    private class SearchTask extends AsyncTask<Connection,PersonItem,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mPersonList!=null) mPersonList.clear();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Connection... params) {
            System.out.println("SEARCH FOR " + textSearch);
                try {
                    ResultSet resultSet = params[0].prepareStatement("SELECT "
                            + SQL_Connection.COLUMN_ALL_STAFF_DIVISION + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_LASTNAME + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_FIRSTNAME + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_MIDNAME + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_SEX + ","
                            + SQL_Connection.COLUMN_ALL_STAFF_TAG
                            + " FROM " + SQL_Connection.ALL_STAFF_TABLE
                            + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_LASTNAME
                            + " LIKE '" + textSearch + "%'").executeQuery();
                    while (resultSet.next()){
                        if (isCancelled()) break;
                        PersonItem personItem = new PersonItem();
                        if (resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG)!=null){
                            ResultSet resultPhoto = params[0].createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                                    .executeQuery("SELECT " + SQL_Connection.COLUMN_ALL_STAFF_PHOTO
                            + " FROM " + SQL_Connection.ALL_STAFF_TABLE
                            + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_TAG
                            + " = '" + resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG) + "'");
                            resultPhoto.first();
                            if (resultPhoto.getRow()!=0){
                                String photo = resultPhoto.getString(SQL_Connection.COLUMN_ALL_STAFF_PHOTO);
                                if (photo == null) photo = FavoriteDB.getBase64DefaultPhotoFromResources();
                                //Сохраняем фото во временную папку. При выходе папка очищается
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

            return null;
        }

        @Override
        protected void onProgressUpdate(PersonItem... values) {
            super.onProgressUpdate(values);
            mPersonList.add(values[0]);
            mPersonsAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
/*
    private class TransportPersonTask extends AsyncTask <String,Void,PersonItem>{

        @Override
        protected PersonItem doInBackground(String... params) {
            return FavoriteDB.getPersonItem(params[0], FavoriteDB.SERVER_USER, true);
        }

        @Override
        protected void onPostExecute(PersonItem personItem) {
            super.onPostExecute(personItem);
            if (personItem!=null){
                addNewUser(personItem);
            }
            if (mSelectedRoom!=null){
                new BaseWriter(mContext, (BaseWriterInterface)getActivity()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new BaseWriterParams()
                        .setAccessType(FavoriteDB.CLICK_USER_ACCESS)
                        .setAuditroom(mSelectedRoom)
                        .setPersonTag(personItem.getRadioLabel()));
            }
        }
    }*/

    private void addNewUser (PersonItem personItem){
        String snackText;
        if (FavoriteDB.addNewUser(personItem, Settings.getWriteServerStatus())){
            snackText = getResources().getString(R.string.snack_user_add_success);
        } else {
            snackText = getResources().getString(R.string.snack_user_add_error);
        }
        Snackbar.make(getView(), snackText, Snackbar.LENGTH_SHORT).show();
    }
}
