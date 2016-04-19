package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.ImageSaver;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.TagSearcherInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

/**
 * Поиск пользователей
 */
public class SearchFr extends Fragment implements TagSearcherInterface, RecycleItemClickListener{

    private Context mContext;
    private TagSearcherInterface mTagSeacherInterface;

    private ArrayList<String> mPersonTagList;
    private ArrayList<PersonItem> mPersonItemsList;

    private TagSearcher tagSearcher;

    private ProgressBar mProgressBar;
    private RecyclerView mPersonsRecycler;
    private AdapterPersonsGrid mAdapter;

    public static SearchFr new_Instance(){
        return new SearchFr();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_search_fr,container,false);
        mContext = rootView.getContext();
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.layout_add_new_staff_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        mTagSeacherInterface = this;

        mPersonItemsList = new ArrayList<>();

        mPersonsRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_search_persons);
        mPersonsRecycler.setLayoutManager(new GridLayoutManager(mContext,3));

        mAdapter = new AdapterPersonsGrid(mContext,mPersonItemsList,AdapterPersonsGrid.SHOW_ALL_PERSONS,this);
        mPersonsRecycler.setAdapter(mAdapter);

        Button mAddButton = (Button) rootView.findViewById(R.id.layout_add_new_staff_input_button);

        final Connection connection = SQL_Connection.SQLconnect;
        final TextInputLayout mInputLayout = (TextInputLayout)rootView.findViewById(R.id.layout_add_new_staff_input);
        final AppCompatEditText mInputText = (AppCompatEditText) rootView.findViewById(R.id.layout_add_new_staff_input_text);
        if (mInputText.requestFocus()){
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        mInputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length()>=3 && s.length()<=6){
                    if (connection!=null){
                        try {
                            if (count != 0){
                                mPersonItemsList.clear();
                                tagSearcher = new TagSearcher(connection, mTagSeacherInterface);
                                tagSearcher.execute(s);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        mInputLayout.setError("Нет подключения к серверу!");
                    }
                } else if (s.length()<3) {
                    mPersonItemsList.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = mInputText.getText().toString();
                if (source.length()==0){
                    mInputLayout.setError(getResources().getString(R.string.input_empty_error));
                }else{
                    String[] split = source.split("\\s+");
                    String lastname = null;
                    String firstname = null;
                    String midname = null;

                    String photo = FavoriteDB.getBase64DefaultPhotoFromResources();
                    try {
                        lastname = split[0];
                        firstname = split[1];
                        midname = split[2];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    lastname = lastname.substring(0,1).toUpperCase() + lastname.substring(1,lastname.length());

                    addUserInFavorite(new PersonItem().setLastname(lastname)
                            .setFirstname(firstname)
                            .setMidname(midname)
                            .setPhoto(photo)
                            .setRadioLabel(String.valueOf(new Random().nextLong() % (100000 - 1)) + 1));
                }
            }
        });
        return rootView;
    }

    @Override
    public void changeProgressBar(int visibility) {
        mProgressBar.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            mPersonsRecycler.setVisibility(View.INVISIBLE);
        }else{
            mPersonsRecycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateGrid(ArrayList<PersonItem> personItems) {

        if (!mPersonItemsList.isEmpty()) mPersonItemsList.clear();
        mPersonItemsList.addAll(personItems);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPersonGet(PersonItem personItem) {

        mPersonItemsList.add(personItem);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((Launcher) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.title_add_new_staff));
        }
    }

    @Override
    public void onItemClick(View v, final int position, int viewID) {
       new PersonTransporterFromServer().execute(mPersonItemsList.get(position).getRadioLabel());
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
    }

    private void addUserInFavorite(final PersonItem personItem){

        FavoriteDB.addNewUser(personItem);

        if (getView()!=null){
            Snackbar.make(getView(),R.string.snack_user_added,Snackbar.LENGTH_LONG)
                    .setAction(R.string.snack_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            FavoriteDB.deleteUser(personItem.getRadioLabel());
                            Snackbar.make(getView(),R.string.snack_cancelled,Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .show();
        }
    }

    //получение информации о пользователе и запись в базу
    private class PersonTransporterFromServer extends AsyncTask<String,Void,PersonItem>{

        @Override
        protected PersonItem doInBackground(String... params) {
            return FavoriteDB.getPersonItem(params[0], FavoriteDB.SERVER_USER);
        }

        @Override
        protected void onPostExecute(PersonItem personItem) {
            if (personItem != null) {
                addUserInFavorite(personItem);
            }
        }
    }

    private class TagSearcher extends AsyncTask<CharSequence,PersonItem,ArrayList<PersonItem>> {

        private TagSearcherInterface mTagSearcherInterface;
        private Connection mConnection;

        public TagSearcher(Connection connection, TagSearcherInterface tagSearcherInterface){
            this.mConnection = connection;
            mTagSearcherInterface = tagSearcherInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTagSearcherInterface.changeProgressBar(View.VISIBLE);
        }


        @Override
        protected ArrayList<PersonItem> doInBackground(CharSequence... params) {
            ArrayList<PersonItem> mItems = new ArrayList<>();
            try {
                Statement statement = mConnection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT "
                        + SQL_Connection.COLUMN_ALL_STAFF_DIVISION + ","
                        + SQL_Connection.COLUMN_ALL_STAFF_LASTNAME + ","
                        + SQL_Connection.COLUMN_ALL_STAFF_FIRSTNAME + ","
                        + SQL_Connection.COLUMN_ALL_STAFF_MIDNAME + ","
                        + SQL_Connection.COLUMN_ALL_STAFF_SEX + ","
                        + SQL_Connection.COLUMN_ALL_STAFF_TAG
                        + " FROM " + SQL_Connection.ALL_STAFF_TABLE
                        + " WHERE " + SQL_Connection.COLUMN_ALL_STAFF_LASTNAME
                        + " LIKE '" + params[0] + "%'");
                while (resultSet.next()){


                   // mItems.add();

                    publishProgress(new PersonItem().setLastname(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_LASTNAME))
                            .setFirstname(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_FIRSTNAME))
                            .setMidname(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_MIDNAME))
                            .setDivision(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_DIVISION))
                            .setRadioLabel(resultSet.getString(SQL_Connection.COLUMN_ALL_STAFF_TAG)));

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return mItems;
        }

        @Override
        protected void onPostExecute(ArrayList<PersonItem> personItems) {
            System.out.println("tag searcher ---------------------------------");
            mTagSearcherInterface.changeProgressBar(View.INVISIBLE);
          //  mTagSearcherInterface.updateGrid(personItems);
        }

        @Override
        protected void onProgressUpdate(PersonItem... values) {
            mTagSearcherInterface.onPersonGet(values[0]);
        }
    }

}
