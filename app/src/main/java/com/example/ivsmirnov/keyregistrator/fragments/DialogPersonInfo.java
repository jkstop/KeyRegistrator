package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.async_tasks.SQL_Connection;
import com.example.ivsmirnov.keyregistrator.async_tasks.ServerWriter;
import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 06.06.2016.
 */
public class DialogPersonInfo extends DialogFragment implements SQL_Connection.Callback {


    public static final String BUNDLE_PERSON = "bundle_person";
    public static final String BUNDLE_POSITION = "bundle_position";

    private static final int DELETE_USER = 1;
    private static final int UPDATE_USER = 2;

    public static final int PERSON_LASTNAME = 0;
    public static final int PERSON_FIRSTNAME = 1;
    public static final int PERSON_MIDNAME = 2;
    public static final int PERSON_DIVISION = 3;
    public static final int PERSON_TAG = 4;
    public static final int PERSON_ACCESS = 5;

    private String[] personBundle;
    private int positionBundle;
    private PersonItem updatablePersonItem;

    private Callback mCallback;
    private SQL_Connection.Callback mConnectCallback;


    public static DialogPersonInfo newInstanse(String[] bundlePerson, int bundlePosition){
        Bundle bundle = new Bundle();
        bundle.putStringArray(BUNDLE_PERSON, bundlePerson);
        bundle.putInt(BUNDLE_POSITION, bundlePosition);
        DialogPersonInfo dialogPersonInfo = new DialogPersonInfo();
        dialogPersonInfo.setArguments(bundle);
        return dialogPersonInfo;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (Callback)getTargetFragment();
        mConnectCallback = this;
        Bundle extras = getArguments();
        if (extras!=null){
            personBundle = extras.getStringArray(BUNDLE_PERSON);
            positionBundle = extras.getInt(BUNDLE_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.view_dialog_person_info, container, false);

        final TextInputLayout textLastname = (TextInputLayout)dialogView.findViewById(R.id.person_info_lastname);
        final TextInputLayout textFirstname = (TextInputLayout)dialogView.findViewById(R.id.person_info_firstname);
        final TextInputLayout textMidname = (TextInputLayout)dialogView.findViewById(R.id.person_info_midname);
        final TextInputLayout textDivision = (TextInputLayout)dialogView.findViewById(R.id.person_info_division);
        final AppCompatCheckBox checkAccess = (AppCompatCheckBox)dialogView.findViewById(R.id.person_info_access);
        ImageView personImage = (ImageView)dialogView.findViewById(R.id.person_info_photo);

        Toolbar toolbar = (Toolbar)dialogView.findViewById(R.id.person_info_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Информация");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
        toolbar.inflateMenu(R.menu.menu_person_info);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_person_info_save:
                        //обновление пользователя
                        int access = FavoriteDB.CARD_USER_ACCESS;
                        if (checkAccess.isChecked()) access = FavoriteDB.CLICK_USER_ACCESS;

                        updatablePersonItem = new PersonItem()
                                .setLastname(textLastname.getEditText().getText().toString())
                                .setFirstname(textFirstname.getEditText().getText().toString())
                                .setMidname(textMidname.getEditText().getText().toString())
                                .setDivision(textDivision.getEditText().getText().toString())
                                .setRadioLabel(personBundle[PERSON_TAG])
                                .setAccessType(access);

                        FavoriteDB.updatePersonItem(personBundle[PERSON_TAG], updatablePersonItem);

                        //обновление на сервере
                        if (Settings.getWriteServerStatus()){
                            SQL_Connection.getConnection(null, UPDATE_USER, mConnectCallback);
                        }

                        getDialog().cancel();

                        //интерфейс
                        mCallback.onUserChanged(positionBundle, updatablePersonItem);
                        return true;
                    case R.id.menu_person_info_delete:
                        //удаление пользователя
                        FavoriteDB.deleteUser(personBundle[PERSON_TAG]);

                        //удаление с сервера
                        if (Settings.getWriteServerStatus()){
                            SQL_Connection.getConnection(null, DELETE_USER, mConnectCallback);
                        }

                        getDialog().cancel();

                        //интерфейс
                        mCallback.onUserDeleted(positionBundle);
                        return true;
                    default:
                        return false;
                }
            }
        });



        try {
            textLastname.getEditText().setText(personBundle[PERSON_LASTNAME]);
            textFirstname.getEditText().setText(personBundle[PERSON_FIRSTNAME]);
            textMidname.getEditText().setText(personBundle[PERSON_MIDNAME]);
            textDivision.getEditText().setText(personBundle[PERSON_DIVISION]);

            if (personBundle[PERSON_ACCESS].equals(String.valueOf(FavoriteDB.CLICK_USER_ACCESS))){
                checkAccess.setChecked(true);
            }

            Picasso.with(getContext())
                    .load(FavoriteDB.getPersonPhotoPath(personBundle[PERSON_TAG]))
                    .placeholder(R.drawable.ic_user_not_found)
                    .fit()
                    .centerCrop()
                    .into(personImage);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return dialogView;
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        switch (callingTask){
            case DELETE_USER:
                new ServerWriter(ServerWriter.DELETE_ONE, new PersonItem().setRadioLabel(personBundle[PERSON_TAG]),null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                break;
            case UPDATE_USER:
                new ServerWriter(ServerWriter.PERSON_UPDATE, updatablePersonItem, null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, connection);
                break;
            default:
                break;
        }

    }

    @Override
    public void onServerConnectException(Exception e) {

    }



    public interface Callback{
        void onUserDeleted(int position);
        void onUserChanged(int position, PersonItem newPerson);
    }

}
