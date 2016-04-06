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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterPersonsGrid;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.async_tasks.TagSearcher;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.interfaces.TagSearcherInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Random;

/**
 * Поиск пользователей
 */
public class SearchFr extends Fragment implements TagSearcherInterface, RecycleItemClickListener{

    private Context mContext;
    private TagSearcherInterface mTagSeacherInterface;

    private ArrayList<String> mPersonTagList;

    private ProgressBar mProgressBar;
    private RecyclerView mPersonsRecycler;

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

        mPersonsRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_search_persons);
        mPersonsRecycler.setLayoutManager(new GridLayoutManager(mContext,3));

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
                Log.d("s",String.valueOf(s.length()));

                Log.d("count", String.valueOf(count));

                if (s.length()>=3 && s.length()<=6){
                    if (connection!=null){
                        try {
                            TagSearcher tagSearcher;
                            if (count != 0){
                                tagSearcher = new TagSearcher(s, mTagSeacherInterface);
                                tagSearcher.execute(connection);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(mContext,"Нет подключения к серверу!",Toast.LENGTH_SHORT).show();
                    }
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

                    String photo = FavoriteDB.getBase64DefaultPhotoFromResources("М");
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
                            .setPhotoPreview(FavoriteDB.getPhotoPreview(photo))
                            .setPhotoOriginal(photo)
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
    public void updateGrid(ArrayList<String> personTagList) {
        if (!personTagList.isEmpty()){
            mPersonTagList = personTagList;
            mPersonsRecycler.setAdapter(new AdapterPersonsGrid(mContext,
                    mPersonTagList,
                    AdapterPersonsGrid.SHOW_ALL_PERSONS,
                    this));
        }
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
       new PersonTransporterFromServer().execute(position);
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
    }

    private void addUserInFavorite(final PersonItem personItem){

        FavoriteDB.writeInDBTeachers(personItem);

        if (Settings.getWriteServerStatus() && Settings.getWriteTeachersStatus()){
            new ServerWriter(personItem).execute(ServerWriter.PERSON_NEW);
        }

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
    private class PersonTransporterFromServer extends AsyncTask<Integer,Void,PersonItem>{

        @Override
        protected PersonItem doInBackground(Integer... params) {
            return FavoriteDB.getPersonItem(mPersonTagList.get(params[0]), FavoriteDB.SERVER_USER, FavoriteDB.ALL_PHOTO);
        }

        @Override
        protected void onPostExecute(PersonItem personItem) {
            if (personItem != null) {
                addUserInFavorite(personItem);
            }
        }
    }

}
