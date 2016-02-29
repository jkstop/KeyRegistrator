package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
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
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_persons_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Find_User_in_SQL_Server;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.SQL_Connector;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ivsmirnov on 04.12.2015.
 */
public class Search_Fragment extends Fragment implements Find_User_in_SQL_Server_Interface, RecycleItemClickListener{

    private Context mContext;
    private Settings mSettings;
    private Find_User_in_SQL_Server_Interface mListener;

    private ArrayList<PersonItem> mPersonItems;

    private DataBaseFavorite mDataBaseFavorite;

    private ProgressBar mProgressBar;
    private RecyclerView mPersonsRecycler;
    private Button mAddButton;

    public static Search_Fragment new_Instance(){
        return new Search_Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_add_new_staff_fragment,container,false);
        mContext = rootView.getContext();
        mSettings = new Settings(mContext);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.layout_add_new_staff_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        if (Launcher.mDataBaseFavorite!=null){
            mDataBaseFavorite = Launcher.mDataBaseFavorite;
        } else {
            mDataBaseFavorite = new DataBaseFavorite(mContext);
        }

        mListener = this;

        mPersonsRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_search_persons);
        mPersonsRecycler.setLayoutManager(new GridLayoutManager(mContext,3));

        mAddButton = (Button)rootView.findViewById(R.id.layout_add_new_staff_input_button);

        final Connection connection = SQL_Connector.SQL_connection;
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
                if (s.length()>=3){
                    if (connection!=null){
                        try {
                            Find_User_in_SQL_Server find_user_in_sql_server = new Find_User_in_SQL_Server(mContext, s,  mListener);
                            find_user_in_sql_server.execute(connection);
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
                    String lastname = Values.EMPTY;
                    String firstname = Values.EMPTY;
                    String midname = Values.EMPTY;

                    String photo = DataBaseFavorite.getBase64DefaultPhotoFromResources(mContext,"М");
                    try {
                        lastname = split[0];
                        firstname = split[1];
                        midname = split[2];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    lastname = lastname.substring(0,1).toUpperCase() + lastname.substring(1,lastname.length());

                    addUserInFavorite(mContext, new PersonItem().setLastname(lastname)
                            .setFirstname(firstname)
                            .setMidname(midname)
                            .setPhotoPreview(DataBaseFavorite.getPhotoPreview(photo))
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
    public void updateGrid(ArrayList<PersonItem> items) {
        if (!items.isEmpty()){
            mPersonItems = items;
            mPersonsRecycler.setAdapter(new adapter_persons_grid(mContext,
                    mPersonItems,
                    Values.SHOW_ALL_PERSONS,
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
    public void onItemClick(View v, int position, int viewID) {

        PersonItem selectedPerson = mDataBaseFavorite.getPersonItem(mPersonItems.get(position).getRadioLabel(), DataBaseFavorite.SERVER_USER, DataBaseFavorite.FULLSIZE_PHOTO);
        selectedPerson.setPhotoPreview(DataBaseFavorite.getPhotoPreview(selectedPerson.getPhotoOriginal()));
        addUserInFavorite(mContext, selectedPerson);
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
    }

    private void addUserInFavorite(final Context context, final PersonItem personItem){

        mDataBaseFavorite.writeInDBTeachers(personItem);


        if (getView()!=null){
            Snackbar.make(getView(),R.string.snack_user_added,Snackbar.LENGTH_LONG)
                    .setAction(R.string.snack_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mDataBaseFavorite.deleteFromTeachersDB(personItem);
                            Snackbar.make(getView(),R.string.snack_cancelled,Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .show();
        }

    }

}
